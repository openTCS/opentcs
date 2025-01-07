// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
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
    implements
      RechargeVehicleSelectionFilter {

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
        && hasAcceptableOrderTypesForCharging(vehicle);
  }

  private boolean hasAcceptableOrderTypesForCharging(Vehicle vehicle) {
    return vehicle.getAcceptableOrderTypes().stream()
        .anyMatch(
            orderType -> orderType.getName().equals(OrderConstants.TYPE_CHARGE)
                || orderType.getName().equals(OrderConstants.TYPE_ANY)
        );
  }
}
