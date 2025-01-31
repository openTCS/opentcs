// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.selection.vehicles;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.selection.ParkVehicleSelectionFilter;
import org.opentcs.util.TimeProvider;

/**
 * Filters vehicles that are parkable.
 */
public class IsParkable
    implements
      ParkVehicleSelectionFilter {

  private final TCSObjectService objectService;
  private final DefaultDispatcherConfiguration configuration;
  private final TimeProvider timeProvider;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service.
   * @param configuration The dispatcher configuration.
   * @param timeProvider Provider to get the current time.
   */
  @Inject
  public IsParkable(
      TCSObjectService objectService,
      DefaultDispatcherConfiguration configuration,
      TimeProvider timeProvider
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.configuration = requireNonNull(configuration, "configuration");
    this.timeProvider = requireNonNull(timeProvider, "timeProvider");
  }

  @Override
  public Collection<String> apply(Vehicle vehicle) {
    return parkable(vehicle) ? new ArrayList<>() : Arrays.asList(getClass().getName());
  }

  private boolean parkable(Vehicle vehicle) {
    return vehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED
        && inProcStateIdleFor(vehicle, configuration.parkIdleVehiclesDelay())
        && vehicle.hasState(Vehicle.State.IDLE)
        && vehicle.getCurrentPosition() != null
        && !isParkingPosition(vehicle.getCurrentPosition())
        && vehicle.getOrderSequence() == null
        && hasAcceptableOrderTypesForParking(vehicle);
  }

  private boolean inProcStateIdleFor(Vehicle vehicle, long millis) {
    if (!vehicle.hasProcState(Vehicle.ProcState.IDLE)) {
      return false;
    }

    return vehicle.getProcStateTimestamp().plusMillis(millis)
        .isBefore(timeProvider.getCurrentTimeInstant());
  }

  private boolean isParkingPosition(TCSObjectReference<Point> positionRef) {
    if (positionRef == null) {
      return false;
    }

    Point position = objectService.fetchObject(Point.class, positionRef);
    return position.isParkingPosition();
  }

  private boolean hasAcceptableOrderTypesForParking(Vehicle vehicle) {
    return vehicle.getAcceptableOrderTypes().stream()
        .anyMatch(
            orderType -> orderType.getName().equals(OrderConstants.TYPE_PARK)
                || orderType.getName().equals(OrderConstants.TYPE_ANY)
        );
  }
}
