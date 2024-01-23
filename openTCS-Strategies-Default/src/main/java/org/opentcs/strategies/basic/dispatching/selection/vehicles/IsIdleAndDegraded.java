/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.selection.vehicles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.strategies.basic.dispatching.selection.RechargeVehicleSelectionFilter;

/**
 * Filters vehicles that are idle and have a degraded energy level.
 */
public class IsIdleAndDegraded
    implements RechargeVehicleSelectionFilter {

  /**
   * Creates a new instance.
   */
  public IsIdleAndDegraded() {
  }

  @Override
  public Collection<String> apply(Vehicle vehicle) {
    return idleAndDegraded(vehicle) ? new ArrayList<>() : Arrays.asList(getClass().getName());
  }

  private boolean idleAndDegraded(Vehicle vehicle) {
    return vehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED
        && vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && vehicle.hasState(Vehicle.State.IDLE)
        && vehicle.getCurrentPosition() != null
        && vehicle.getOrderSequence() == null
        && vehicle.isEnergyLevelDegraded()
        && hasAllowedOrderTypesForCharging(vehicle);
  }
  
  private boolean hasAllowedOrderTypesForCharging(Vehicle vehicle) {
    return vehicle.getAllowedOrderTypes().contains(OrderConstants.TYPE_CHARGE)
        || vehicle.getAllowedOrderTypes().contains(OrderConstants.TYPE_ANY);
  }
}
