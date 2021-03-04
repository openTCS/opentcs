/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling;

import static com.google.common.base.Preconditions.checkPositionIndex;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.components.kernel.ResourceAllocationException;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.TCSResource;
import org.opentcs.strategies.basic.scheduling.AllocatorCommand.Allocate;
import org.opentcs.strategies.basic.scheduling.AllocatorCommand.AllocationsReleased;
import org.opentcs.strategies.basic.scheduling.AllocatorCommand.CheckAllocationsPrepared;
import org.opentcs.strategies.basic.scheduling.AllocatorCommand.RetryAllocates;
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
    implements Scheduler {

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
   * All claims.
   */
  private final Map<Client, List<Set<TCSResource<?>>>> claimsByClient = new HashMap<>();
  /**
   * The reservation pool.
   */
  private final ReservationPool reservationPool = new ReservationPool();
  /**
   * Allocations deferred because they couldn't be granted, yet.
   */
  private final Queue<AllocatorCommand.Allocate> deferredAllocations = new LinkedBlockingQueue<>();
  /**
   * Executes scheduling tasks.
   */
  private final ScheduledExecutorService kernelExecutor;
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;

  /**
   * Creates a new BasicScheduler instance.
   *
   * @param plantModelService The plant model service.
   * @param allocationAdvisor Takes care of modules.
   * @param kernelExecutor Executes scheduling tasks.
   */
  @Inject
  public DefaultScheduler(InternalPlantModelService plantModelService,
                          AllocationAdvisor allocationAdvisor,
                          @KernelExecutor ScheduledExecutorService kernelExecutor) {
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
    this.allocationAdvisor = requireNonNull(allocationAdvisor, "allocationAdvisor");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    reservationPool.clear();
    allocationAdvisor.initialize();

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

    allocationAdvisor.terminate();
    initialized = false;
  }

  @Override
  public void claim(Client client, List<Set<TCSResource<?>>> resources) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    synchronized (reservationPool) {
      claimsByClient.put(client, resources);

      allocationAdvisor.claim(client, resources);
      allocationAdvisor.setAllocationState(client,
                                           reservationPool.allocatedResources(client),
                                           resources);
    }
  }

  @Override
  public void updateProgressIndex(Client client, int index) {
    requireNonNull(client, "client");
    checkPositionIndex(index, Integer.MAX_VALUE, "index");

    if (index == 0) {
      return;
    }
    // XXX Verify that the index is only incremented, never decremented?

    synchronized (reservationPool) {
      List<Set<TCSResource<?>>> claims = claimsByClient.get(client);
      List<Set<TCSResource<?>>> remainingClaims = claims.subList(index, claims.size());
      allocationAdvisor.setAllocationState(client,
                                           reservationPool.allocatedResources(client),
                                           remainingClaims);
    }
  }

  @Override
  public void unclaim(Client client) {
    requireNonNull(client, "client");

    synchronized (reservationPool) {
      claimsByClient.remove(client);

      allocationAdvisor.setAllocationState(client,
                                           reservationPool.allocatedResources(client),
                                           new LinkedList<>());
      allocationAdvisor.unclaim(client);
    }
  }

  @Override
  public void allocate(Client client, Set<TCSResource<?>> resources) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    kernelExecutor.submit(new AllocatorTask(plantModelService,
                                            reservationPool,
                                            deferredAllocations,
                                            allocationAdvisor,
                                            kernelExecutor,
                                            new Allocate(client, resources)));
  }

  @Override
  public void allocateNow(Client client, Set<TCSResource<?>> resources)
      throws ResourceAllocationException {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    synchronized (reservationPool) {
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

    synchronized (reservationPool) {
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
                                              new AllocationsReleased(client,
                                                                      completelyFreeResources)));
    }
    kernelExecutor.submit(new AllocatorTask(plantModelService,
                                            reservationPool,
                                            deferredAllocations,
                                            allocationAdvisor,
                                            kernelExecutor,
                                            new RetryAllocates(client)));
  }

  @Override
  public void freeAll(Client client) {
    requireNonNull(client, "client");

    synchronized (reservationPool) {
      LOG.debug("{}: Releasing all resources", client.getId());
      reservationPool.freeAll(client);
    }
    kernelExecutor.submit(new AllocatorTask(plantModelService,
                                            reservationPool,
                                            deferredAllocations,
                                            allocationAdvisor,
                                            kernelExecutor,
                                            new RetryAllocates(client)));
  }

  @Override
  public Map<String, Set<TCSResource<?>>> getAllocations() {
    synchronized (reservationPool) {
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
                                            new CheckAllocationsPrepared(client, resources)));
  }
}
