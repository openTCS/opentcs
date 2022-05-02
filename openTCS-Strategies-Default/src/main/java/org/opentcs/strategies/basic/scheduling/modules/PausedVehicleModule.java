/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling.modules;

import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows allocations for vehicles that are not paused only.
 * <p>
 * Note that this module assumes that a client's {@link Scheduler.Client#getId()} returns the name
 * of a vehicle.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PausedVehicleModule
    implements Scheduler.Module {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PausedVehicleModule.class);
  /**
   * The object service.
   */
  private final TCSObjectService objectService;
  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * Whether this module is initialized.
   */
  private boolean initialized;

  @Inject
  public PausedVehicleModule(@Nonnull TCSObjectService objectService,
                             @Nonnull @GlobalSyncObject Object globalSyncObject) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

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

    initialized = false;
  }

  @Override
  public void setAllocationState(Scheduler.Client client,
                                 Set<TCSResource<?>> alloc,
                                 List<Set<TCSResource<?>>> remainingClaim) {
  }

  @Override
  public boolean mayAllocate(Scheduler.Client client,
                             Set<TCSResource<?>> resources) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    synchronized (globalSyncObject) {
      Vehicle vehicle = objectService.fetchObject(Vehicle.class, client.getId());

      if (vehicle == null) {
        LOG.debug("Client '{}' is not a vehicle; not interfering with allocation.", client.getId());
        return true;
      }
      if (!vehicle.isPaused()) {
        return true;
      }

      LOG.debug("Not allowing allocation for paused vehicle '{}'.", client.getId());
      return false;
    }
  }

  @Override
  public void prepareAllocation(Scheduler.Client client,
                                Set<TCSResource<?>> resources) {
  }

  @Override
  public boolean hasPreparedAllocation(Scheduler.Client client,
                                       Set<TCSResource<?>> resources) {
    return true;
  }

  @Override
  public void allocationReleased(Scheduler.Client client,
                                 Set<TCSResource<?>> resources) {
  }
}
