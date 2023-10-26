/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling.modules.areaAllocation;

import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.scheduling.ReservationPool;

/**
 * Allows resource allocation only if the area related to the respective resource is free.
 */
public class AreaAllocationModule
    implements Scheduler.Module {

  private final TCSObjectService objectService;
  private final ReservationPool reservationPool;
  private final AreaAllocator areaAllocator;
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service to use.
   * @param reservationPool Keeps track of allocated resources.
   * @param areaAllocator Keeps track of allocated areas.
   */
  @Inject
  public AreaAllocationModule(@Nonnull TCSObjectService objectService,
                              @Nonnull ReservationPool reservationPool,
                              @Nonnull AreaAllocator areaAllocator) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.reservationPool = requireNonNull(reservationPool, "reservationPool");
    this.areaAllocator = requireNonNull(areaAllocator, "areaAllocator");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    areaAllocator.initialize();

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

    areaAllocator.terminate();

    initialized = false;
  }

  @Override
  public void setAllocationState(@Nonnull Scheduler.Client client,
                                 @Nonnull Set<TCSResource<?>> alloc,
                                 @Nonnull List<Set<TCSResource<?>>> remainingClaim) {
    allocationChanged(client);
  }

  @Override
  public boolean mayAllocate(@Nonnull Scheduler.Client client,
                             @Nonnull Set<TCSResource<?>> resources) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    if (client.getRelatedVehicle() == null) {
      return true;
    }

    Vehicle vehicle = objectService.fetchObject(Vehicle.class, client.getRelatedVehicle());
    if (vehicle.getEnvelopeKey() == null) {
      return true;
    }

    return areaAllocator.mayAllocateAreas(client.getRelatedVehicle(),
                                          vehicle.getEnvelopeKey(),
                                          resources);
  }

  @Override
  public void prepareAllocation(@Nonnull Scheduler.Client client,
                                @Nonnull Set<TCSResource<?>> resources) {
  }

  @Override
  public boolean hasPreparedAllocation(@Nonnull Scheduler.Client client,
                                       @Nonnull Set<TCSResource<?>> resources) {
    return true;
  }

  @Override
  public void allocationReleased(@Nonnull Scheduler.Client client,
                                 @Nonnull Set<TCSResource<?>> resources) {
    allocationChanged(client);
  }

  private void allocationChanged(@Nonnull Scheduler.Client client) {
    requireNonNull(client, "client");

    if (client.getRelatedVehicle() == null) {
      return;
    }

    Vehicle vehicle = objectService.fetchObject(Vehicle.class, client.getRelatedVehicle());
    if (vehicle.getEnvelopeKey() == null) {
      return;
    }

    areaAllocator.updateAllocatedAreas(client.getRelatedVehicle(),
                                       vehicle.getEnvelopeKey(),
                                       reservationPool.allocatedResources(client));
  }
}
