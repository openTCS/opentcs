/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.components.kernel.ResourceAllocationException;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.scheduling.AllocatorCommand.Allocate;
import org.opentcs.strategies.basic.scheduling.AllocatorCommand.AllocationsReleased;
import org.opentcs.strategies.basic.scheduling.AllocatorCommand.CheckAllocationsPrepared;
import org.opentcs.strategies.basic.scheduling.AllocatorCommand.RetryAllocates;
import static org.opentcs.util.Assertions.checkArgument;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a basic simple scheduler strategy for resources used by vehicles, preventing
 * collisions.
 *
 * @author Iryna Felko (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultScheduler
    implements Scheduler,
               EventHandler {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultScheduler.class);
  /**
   * The plant model service.
   */
  private final InternalPlantModelService plantModelService;
  /**
   * Takes care of modules.
   */
  private final Module allocationAdvisor;
  /**
   * The reservation pool.
   */
  private final ReservationPool reservationPool;
  /**
   * Allocations deferred because they couldn't be granted, yet.
   */
  private final Queue<AllocatorCommand.Allocate> deferredAllocations = new LinkedBlockingQueue<>();
  /**
   * Executes scheduling tasks.
   */
  private final ScheduledExecutorService kernelExecutor;
  /**
   * The kernel's event bus.
   */
  private final EventBus eventBus;
  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * Allocations that are scheduled for execution on the kernel executor.
   */
  private final Map<Client, List<Future<?>>> allocateFutures = new HashMap<>();
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;

  /**
   * Creates a new BasicScheduler instance.
   *
   * @param plantModelService The plant model service.
   * @param allocationAdvisor Takes care of modules.
   * @param reservationPool The reservation pool to be used.
   * @param kernelExecutor Executes scheduling tasks.
   * @param eventBus The kernel's event bus.
   * @param globalSyncObject The kernel threads' global synchronization object.
   */
  @Inject
  public DefaultScheduler(InternalPlantModelService plantModelService,
                          AllocationAdvisor allocationAdvisor,
                          ReservationPool reservationPool,
                          @KernelExecutor ScheduledExecutorService kernelExecutor,
                          @ApplicationEventBus EventBus eventBus,
                          @GlobalSyncObject Object globalSyncObject) {
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
    this.allocationAdvisor = requireNonNull(allocationAdvisor, "allocationAdvisor");
    this.reservationPool = requireNonNull(reservationPool, "reservationPool");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    reservationPool.clear();
    allocationAdvisor.initialize();

    eventBus.subscribe(this);

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    eventBus.unsubscribe(this);

    allocationAdvisor.terminate();

    initialized = false;
  }

  @Override
  public void claim(Client client, List<Set<TCSResource<?>>> resources) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    synchronized (globalSyncObject) {
      reservationPool.setClaim(client, resources);

      allocationAdvisor.setAllocationState(client,
                                           reservationPool.allocatedResources(client),
                                           resources);
    }
  }

  @Override
  @Deprecated
  public void unclaim(Client client) {
    requireNonNull(client, "client");

    synchronized (globalSyncObject) {
      reservationPool.setClaim(client, new LinkedList<>());

      allocationAdvisor.setAllocationState(client,
                                           reservationPool.allocatedResources(client),
                                           new LinkedList<>());
    }
  }

  @Override
  public void allocate(Client client, Set<TCSResource<?>> resources) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    synchronized (globalSyncObject) {
      checkArgument(reservationPool.isNextInClaim(client, resources),
                    "Not the next claimed resources: %s",
                    resources);

      Future<?> allocateFuture = kernelExecutor.submit(
          new AllocatorTask(plantModelService,
                            reservationPool,
                            deferredAllocations,
                            allocationAdvisor,
                            kernelExecutor,
                            globalSyncObject,
                            new Allocate(client, resources))
      );

      // Remember the allocate future in case we need to cancel it.
      addAllocateFuture(client, allocateFuture);

      // Clean up the collection of allocate futures and remove futures that have already been 
      // completed. This could also be done in other places, but doing it for every new allocation 
      // should be sufficient.
      removeCompletedAllocateFutures(client);
    }
  }

  @Override
  public void allocateNow(Client client, Set<TCSResource<?>> resources)
      throws ResourceAllocationException {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    synchronized (globalSyncObject) {
      // Check if all resources are available.
      final Set<TCSResource<?>> availableResources = new HashSet<>();
      for (TCSResource<?> curResource : resources) {
        ReservationEntry entry = reservationPool.getReservationEntry(curResource);
        if (!entry.isFree() && !entry.isAllocatedBy(client)) {
          LOG.warn("{}: Resource {} unavailable, reserved by {}",
                   client.getId(),
                   curResource.getName(),
                   entry.getClient().getId());
          // XXX DO something about it?!
        }
        else {
          availableResources.add(curResource);
        }
      }
      // Allocate all requested resources that are available.
      LOG.debug("{}: Allocating immediately: {}", client.getId(), availableResources);
      for (TCSResource<?> curResource : availableResources) {
        reservationPool.getReservationEntry(curResource).allocate(client);
      }
    }
  }

  @Override
  public void free(Client client, Set<TCSResource<?>> resources) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    synchronized (globalSyncObject) {
      LOG.debug("{}: Releasing resources: {}", client.getId(), resources);
      reservationPool.free(client, resources);

      // Check which resources are now completely free
      Set<TCSResource<?>> completelyFreeResources = resources.stream()
          .filter(resource -> reservationPool.getReservationEntry(resource).isFree())
          .collect(Collectors.toCollection(HashSet::new));
      kernelExecutor.submit(new AllocatorTask(plantModelService,
                                              reservationPool,
                                              deferredAllocations,
                                              allocationAdvisor,
                                              kernelExecutor,
                                              globalSyncObject,
                                              new AllocationsReleased(client,
                                                                      completelyFreeResources)));
    }
    kernelExecutor.submit(new AllocatorTask(plantModelService,
                                            reservationPool,
                                            deferredAllocations,
                                            allocationAdvisor,
                                            kernelExecutor,
                                            globalSyncObject,
                                            new RetryAllocates(client)));
  }

  @Override
  public void freeAll(Client client) {
    requireNonNull(client, "client");

    synchronized (globalSyncObject) {
      Set<TCSResource<?>> freedResources = reservationPool.allocatedResources(client);

      LOG.debug("{}: Releasing all resources...", client.getId());
      reservationPool.freeAll(client);
      clearPendingAllocations(client);

      kernelExecutor.submit(new AllocatorTask(plantModelService,
                                              reservationPool,
                                              deferredAllocations,
                                              allocationAdvisor,
                                              kernelExecutor,
                                              globalSyncObject,
                                              new AllocationsReleased(client,
                                                                      freedResources)));
    }
    kernelExecutor.submit(new AllocatorTask(plantModelService,
                                            reservationPool,
                                            deferredAllocations,
                                            allocationAdvisor,
                                            kernelExecutor,
                                            globalSyncObject,
                                            new RetryAllocates(client)));
  }

  @Override
  public void clearPendingAllocations(Client client) {
    requireNonNull(client, "client");
    synchronized (globalSyncObject) {
      LOG.debug("{}: Clearing pending allocation requests...", client.getId());
      deferredAllocations.removeIf(allocate -> client.equals(allocate.getClient()));
      cancelPendingAllocateFutures(client);
    }
  }

  @Override
  public void reschedule() {
    kernelExecutor.submit(new AllocatorTask(plantModelService,
                                            reservationPool,
                                            deferredAllocations,
                                            allocationAdvisor,
                                            kernelExecutor,
                                            globalSyncObject,
                                            new RetryAllocates(new DummyClient())));
  }

  @Override
  public Map<String, Set<TCSResource<?>>> getAllocations() {
    synchronized (globalSyncObject) {
      return reservationPool.getAllocations();
    }
  }

  @Override
  public void preparationSuccessful(@Nonnull Module module,
                                    @Nonnull Client client,
                                    @Nonnull Set<TCSResource<?>> resources) {
    requireNonNull(module, "module");
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    kernelExecutor.submit(new AllocatorTask(plantModelService,
                                            reservationPool,
                                            deferredAllocations,
                                            allocationAdvisor,
                                            kernelExecutor,
                                            globalSyncObject,
                                            new CheckAllocationsPrepared(client, resources)));
  }

  @Override
  public void onEvent(Object event) {
    if (!(event instanceof TCSObjectEvent)) {
      return;
    }

    TCSObjectEvent tcsObjectEvent = (TCSObjectEvent) event;
    if (tcsObjectEvent.getType() != TCSObjectEvent.Type.OBJECT_MODIFIED
        || !(tcsObjectEvent.getCurrentObjectState() instanceof Vehicle)) {
      return;
    }

    // If the vehicle was unpaused, trigger a scheduling run in case the vehicle is waiting for
    // resources.
    if (((Vehicle) tcsObjectEvent.getPreviousObjectState()).isPaused()
        && !((Vehicle) tcsObjectEvent.getCurrentObjectState()).isPaused()) {
      reschedule();
    }
  }

  private void addAllocateFuture(Client client, Future<?> allocateFuture) {
    if (!allocateFutures.containsKey(client)) {
      allocateFutures.put(client, new ArrayList<>());
    }

    allocateFutures.get(client).add(allocateFuture);
  }

  private void removeCompletedAllocateFutures(Client client) {
    if (!allocateFutures.containsKey(client)) {
      return;
    }

    allocateFutures.get(client).removeAll(
        allocateFutures.get(client).stream()
            .filter(future -> future.isDone())
            .collect(Collectors.toList())
    );
  }

  private void cancelPendingAllocateFutures(Client client) {
    if (!allocateFutures.containsKey(client)) {
      return;
    }

    allocateFutures.get(client).stream()
        .filter(future -> !future.isDone())
        .forEach(future -> future.cancel(false));
  }

  /**
   * A dummy client for cases in which we need to provide a client but do not have a real one.
   */
  private static class DummyClient
      implements Scheduler.Client {

    @Override
    public String getId() {
      return "DefaultScheduler-DummyClient";
    }

    @Override
    public boolean allocationSuccessful(Set<TCSResource<?>> resources) {
      return false;
    }

    @Override
    public void allocationFailed(Set<TCSResource<?>> resources) {
    }
  }
}
