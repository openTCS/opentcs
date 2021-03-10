/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.selection;

import org.opentcs.data.model.Vehicle;

/**
 * Filters vehicles that are idle and have a degraded energy level.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class FilterVehiclesIdleAndDegraded
    implements RechargeVehicleSelectionFilter {

  @Override
  public boolean test(Vehicle vehicle) {
    return idleAndDegraded(vehicle);
  }

  private boolean idleAndDegraded(Vehicle vehicle) {
    return vehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED
        && vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && vehicle.hasState(Vehicle.State.IDLE)
        && vehicle.getCurrentPosition() != null
        && vehicle.getOrderSequence() == null
        && vehicle.isEnergyLevelDegraded();
  }
}
