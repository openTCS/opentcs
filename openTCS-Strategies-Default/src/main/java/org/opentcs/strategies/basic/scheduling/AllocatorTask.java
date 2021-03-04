/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling;

import static java.util.Objects.requireNonNull;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import javax.annotation.Nonnull;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.Scheduler.Client;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.model.TCSResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles regular resource allocations.
 */
class AllocatorTask
    implements Runnable {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AllocatorTask.class);
  /**
   * The plant model service.
   */
  private final InternalPlantModelService plantModelService;
  /**
   * The reservation pool.
   */
  private final ReservationPool reservationPool;
  /**
   * Takes care of (sub)modules.
   */
  private final Scheduler.Module allocationAdvisor;
  /**
   * Allocations deferred because they couldn't be granted, yet.
   */
  private final Queue<AllocatorCommand.Allocate> deferredAllocations;
  /**
   * Executes tasks.
   */
  private final ScheduledExecutorService kernelExecutor;
  /**
   * Describes the actual task.
   */
  private final AllocatorCommand command;

  /**
   * Creates a new instance.
   */
  public AllocatorTask(@Nonnull InternalPlantModelService plantModelService,
                       @Nonnull ReservationPool reservationPool,
                       @Nonnull Queue<AllocatorCommand.Allocate> deferredAllocations,
                       @Nonnull Scheduler.Module allocationAdvisor,
                       @Nonnull ScheduledExecutorService kernelExecutor,
                       @Nonnull AllocatorCommand command) {
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
    this.reservationPool = requireNonNull(reservationPool, "reservationPool");
    this.deferredAllocations = requireNonNull(deferredAllocations, "deferredAllocations");
    this.allocationAdvisor = requireNonNull(allocationAdvisor, "allocationAdvisor");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.command = requireNonNull(command, "command");
  }

  @Override
  public void run() {
    if (command instanceof AllocatorCommand.Allocate) {
      processAllocate((AllocatorCommand.Allocate) command);
    }
    else if (command instanceof AllocatorCommand.RetryAllocates) {
      scheduleRetryWaitingAllocations();
    }
    else if (command instanceof AllocatorCommand.CheckAllocationsPrepared) {
      checkAllocationsPrepared((AllocatorCommand.CheckAllocationsPrepared) command);
    }
    else if (command instanceof AllocatorCommand.AllocationsReleased) {
      allocationsReleased((AllocatorCommand.AllocationsReleased) command);
    }
    else {
      LOG.warn("Unhandled AllocatorCommand implementation {}, ignored.", command.getClass());
    }
  }

  private void processAllocate(AllocatorCommand.Allocate command) {
    if (!tryAllocate(command)) {
      LOG.debug("{}: Resources unavailable, deferring allocation...", command.getClient().getId());
      deferredAllocations.add(command);
      return;
    }

    checkAllocationsPrepared(command.getClient(), command.getResources());
  }

  private void checkAllocationsPrepared(AllocatorCommand.CheckAllocationsPrepared command) {
    checkAllocationsPrepared(command.getClient(), command.getResources());
  }

  private void checkAllocationsPrepared(Client client, Set<TCSResource<?>> resources) {
    if (!allocationAdvisor.hasPreparedAllocation(client, resources)) {
      LOG.debug("{}: Preparation of resources not yet done.",
                client.getId());
      // XXX remember the resources a client is waiting for preparation done?
      return;
    }

    LOG.debug("Preparation of resources '{}' successful, calling back client '{}'...",
              resources,
              client.getId());
    if (!client.allocationSuccessful(resources)) {
      LOG.warn("{}: Client didn't want allocated resources ({}), unallocating them...",
               client.getId(),
               resources);
      undoAllocate(client, resources);
      // See if others want the resources this one didn't, then.
      scheduleRetryWaitingAllocations();
    }
  }

  /**
   * Allocates the given set of resources, if possible.
   *
   * @param command Describes the requested allocation.
   * @return <code>true</code> if, and only if, the given resources were allocated.
   */
  private boolean tryAllocate(AllocatorCommand.Allocate command) {
    Scheduler.Client client = command.getClient();
    Set<TCSResource<?>> resources = command.getResources();

    synchronized (reservationPool) {
      LOG.debug("{}: Checking resource if all resources are available:", client.getId());
      if (!reservationPool.resourcesAvailableForUser(resources, client)) {
        LOG.debug("{}: Resources unavailable: {}", client.getId(), resources);
        return false;
      }

      LOG.debug("{}: Checking if resources may be allocated...", client.getId());
      if (!allocationAdvisor.mayAllocate(client, resources)) {
        LOG.debug("{}: Resource allocation restricted by some modules.", client);
        return false;
      }

      LOG.debug("{}: Some resources need to be prepared for allocation.", client.getId());
      allocationAdvisor.prepareAllocation(client, resources);

      LOG.debug("{}: All resources available, allocating...", client.getId());
      // Allocate resources.
      for (TCSResource<?> curRes : command.getResources()) {
        reservationPool.getReservationEntry(curRes).allocate(client);
      }

      return true;
    }
  }

  private void allocationsReleased(AllocatorCommand.AllocationsReleased command) {
    allocationAdvisor.allocationReleased(command.getClient(), command.getResources());
  }

  /**
   * Unallocates the given set of resources.
   *
   * @param command Describes the allocated resources.
   */
  private void undoAllocate(Client client, Set<TCSResource<?>> resources) {
    synchronized (reservationPool) {
      reservationPool.free(client, resources);
    }
  }

  /**
   * Moves all waiting allocations back into the incoming queue so they can be rechecked.
   */
  private void scheduleRetryWaitingAllocations() {
    for (AllocatorCommand.Allocate allocate : deferredAllocations) {
      kernelExecutor.submit(new AllocatorTask(plantModelService,
                                              reservationPool,
                                              deferredAllocations,
                                              allocationAdvisor,
                                              kernelExecutor,
                                              allocate));
    }
    deferredAllocations.clear();
  }
}
