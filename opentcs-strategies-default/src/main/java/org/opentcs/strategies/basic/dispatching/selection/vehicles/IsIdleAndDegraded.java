// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.selection.vehicles;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.selection.RechargeVehicleSelectionFilter;
import org.opentcs.util.TimeProvider;

/**
 * Filters vehicles that are idle and have a degraded energy level.
 */
public class IsIdleAndDegraded
    implements
      RechargeVehicleSelectionFilter {

  private final DefaultDispatcherConfiguration configuration;
  private final TimeProvider timeProvider;

  /**
   * Creates a new instance.
   *
   * @param configuration The dispatcher configuration.
   * @param timeProvider Provider to get the current time.
   */
  @Inject
  public IsIdleAndDegraded(
      DefaultDispatcherConfiguration configuration,
      TimeProvider timeProvider
  ) {
    this.configuration = requireNonNull(configuration, "configuration");
    this.timeProvider = requireNonNull(timeProvider, "timeProvider");
  }

  @Override
  public Collection<String> apply(Vehicle vehicle) {
    return idleAndDegraded(vehicle) ? new ArrayList<>() : Arrays.asList(getClass().getName());
  }

  private boolean idleAndDegraded(Vehicle vehicle) {
    return vehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED
        && inProcStateIdleFor(vehicle, configuration.rechargeIdleVehiclesDelay())
        && vehicle.hasState(Vehicle.State.IDLE)
        && vehicle.getCurrentPosition() != null
        && vehicle.getOrderSequence() == null
        && vehicle.isEnergyLevelDegraded()
        && hasAcceptableOrderTypesForCharging(vehicle);
  }

  private boolean inProcStateIdleFor(Vehicle vehicle, long millis) {
    if (!vehicle.hasProcState(Vehicle.ProcState.IDLE)) {
      return false;
    }

    return vehicle.getProcStateTimestamp().plusMillis(millis)
        .isBefore(timeProvider.getCurrentTimeInstant());
  }

  private boolean hasAcceptableOrderTypesForCharging(Vehicle vehicle) {
    return vehicle.getAcceptableOrderTypes().stream()
        .anyMatch(
            orderType -> orderType.getName().equals(OrderConstants.TYPE_CHARGE)
                || orderType.getName().equals(OrderConstants.TYPE_ANY)
        );
  }
}
