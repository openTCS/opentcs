// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.scheduling;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.Scheduler.Client;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.model.TCSResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles regular resource allocations.
 */
class AllocatorTask
    implements
      Runnable {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AllocatorTask.class);
  /**
   * The reservation pool.
   */
  private final ReservationPool reservationPool;
  /**
   * Takes care of (sub)modules.
   */
  private final Scheduler.Module allocationAdvisor;
  /**
   * The pending allocation manager.
   */
  private final PendingAllocationManager pendingAllocationManager;
  /**
   * Executes tasks.
   */
  private final ScheduledExecutorService kernelExecutor;
  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * Describes the actual task.
   */
  private final AllocatorCommand command;

  /**
   * Creates a new instance.
   */
  AllocatorTask(
      @Nonnull
      ReservationPool reservationPool,
      @Nonnull
      PendingAllocationManager allocationTracker,
      @Nonnull
      Scheduler.Module allocationAdvisor,
      @Nonnull
      ScheduledExecutorService kernelExecutor,
      @Nonnull
      @GlobalSyncObject
      Object globalSyncObject,
      @Nonnull
      AllocatorCommand command
  ) {
    this.reservationPool = requireNonNull(reservationPool, "reservationPool");
    this.pendingAllocationManager
        = requireNonNull(allocationTracker, "pendingAllocationManager");
    this.allocationAdvisor = requireNonNull(allocationAdvisor, "allocationAdvisor");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.command = requireNonNull(command, "command");
  }

  @Override
  public void run() {
    LOG.debug("Processing AllocatorCommand: {}", command);

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
      pendingAllocationManager.addDeferredAllocation(command);
      return;
    }

    checkAllocationsPrepared(command.getClient(), command.getResources());
  }

  private void checkAllocationsPrepared(AllocatorCommand.CheckAllocationsPrepared command) {
    checkAllocationsPrepared(command.getClient(), command.getResources());
  }

  private void checkAllocationsPrepared(Client client, Set<TCSResource<?>> resources) {
    if (!allocationAdvisor.hasPreparedAllocation(client, resources)) {
      LOG.debug(
          "{}: Preparation of resources not yet done.",
          client.getId()
      );
      // XXX remember the resources a client is waiting for preparation done?
      return;
    }

    LOG.debug(
        "Preparation of resources '{}' successful, calling back client '{}'...",
        resources,
        client.getId()
    );
    if (!client.onAllocation(resources)) {
      LOG.warn(
          "{}: Client didn't want allocated resources ({}), unallocating them...",
          client.getId(),
          resources
      );
      undoAllocate(client, resources);
      // See if others want the resources this one didn't, then.
      scheduleRetryWaitingAllocations();
    }
    // Notify modules about the changes in claimed/allocated resources for this client.
    allocationAdvisor.setAllocationState(
        client,
        reservationPool.allocatedResources(client),
        reservationPool.getClaim(client)
    );
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

    synchronized (globalSyncObject) {
      if (!reservationPool.isNextInClaim(client, resources)) {
        LOG.error(
            "{}: Not allocating resources that are not next claimed resources: {}",
            client.getId(),
            resources
        );
        return false;
      }

      LOG.debug("{}: Checking resource availability: {}...", client.getId(), resources);
      if (!reservationPool.resourcesAvailableForUser(resources, client)) {
        LOG.debug("{}: Resources unavailable.", client.getId());
        return false;
      }

      LOG.debug("{}: Checking if resources may be allocated...", client.getId());
      if (!allocationAdvisor.mayAllocate(client, resources)) {
        LOG.debug("{}: Resources may not be allocated.", client.getId());
        return false;
      }

      LOG.debug("{}: Preparing resources for allocation...", client.getId());
      allocationAdvisor.prepareAllocation(client, resources);

      LOG.debug("{}: All resources available, allocating...", client.getId());
      // Allocate resources.
      for (TCSResource<?> curRes : command.getResources()) {
        reservationPool.getReservationEntry(curRes).allocate(client);
      }

      LOG.debug("{}: Removing resources claim: {}...", client.getId(), resources);
      reservationPool.unclaim(client, resources);

      return true;
    }
  }

  private void allocationsReleased(AllocatorCommand.AllocationsReleased command) {
    allocationAdvisor.allocationReleased(command.getClient(), command.getResources());
  }

  /**
   * Unallocates the given set of resources.
   * <p>
   * Note that this does <em>not</em> return any previously claimed resources to the client!
   * </p>
   *
   * @param command Describes the allocated resources.
   */
  private void undoAllocate(Client client, Set<TCSResource<?>> resources) {
    synchronized (globalSyncObject) {
      reservationPool.free(client, resources);
    }
  }

  /**
   * Moves all waiting allocations back into the incoming queue so they can be rechecked.
   */
  private void scheduleRetryWaitingAllocations() {
    for (AllocatorCommand.Allocate allocate : pendingAllocationManager.drainDeferredAllocations()) {
      Future<?> future = kernelExecutor.submit(
          new AllocatorTask(
              reservationPool,
              pendingAllocationManager,
              allocationAdvisor,
              kernelExecutor,
              globalSyncObject,
              allocate
          )
      );
      pendingAllocationManager.addAllocationFuture(allocate.getClient(), future);
    }
  }
}
