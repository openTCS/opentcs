// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.rerouting;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.DriveOrderRouteAssigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ReroutingStrategy} implementation for {@link ReroutingType#REGULAR}.
 * <p>
 * Reroutes a {@link Vehicle} from its future or current position according to
 * {@link VehiclePositionResolver#getFutureOrCurrentPosition(org.opentcs.data.model.Vehicle)}.
 */
public class RegularReroutingStrategy
    extends
      AbstractReroutingStrategy
    implements
      ReroutingStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(RegularReroutingStrategy.class);
  private final VehiclePositionResolver vehiclePositionResolver;

  @Inject
  public RegularReroutingStrategy(
      TCSObjectService objectService,
      RegularDriveOrderMerger driveOrderMerger,
      VehiclePositionResolver vehiclePositionResolver,
      DriveOrderRouteAssigner driveOrderRouteAssigner
  ) {
    super(objectService, driveOrderMerger, driveOrderRouteAssigner);
    this.vehiclePositionResolver = requireNonNull(
        vehiclePositionResolver,
        "vehiclePositionResolver"
    );
  }

  @Override
  public Optional<List<DriveOrder>> reroute(Vehicle vehicle) {
    if (!isVehicleAtExpectedPosition(vehicle)) {
      LOG.warn(
          "Can't perform regular rerouting for {} located at unexpected position.",
          vehicle.getName()
      );
      return Optional.empty();
    }

    return super.reroute(vehicle);
  }

  @Override
  protected Optional<Point> determineRerouteSource(Vehicle vehicle) {
    return Optional.of(vehiclePositionResolver.getFutureOrCurrentPosition(vehicle));
  }

  private boolean isVehicleAtExpectedPosition(Vehicle vehicle) {
    TransportOrder currentTransportOrder
        = getObjectService().fetchObject(TransportOrder.class, vehicle.getTransportOrder());
    TCSObjectReference<Point> currentVehiclePosition = vehicle.getCurrentPosition();
    DriveOrder currentDriveOrder = currentTransportOrder.getCurrentDriveOrder();
    if (currentVehiclePosition == null || currentDriveOrder == null) {
      return false;
    }

    int routeProgressIndex = currentTransportOrder.getCurrentRouteStepIndex();
    if (routeProgressIndex == TransportOrder.ROUTE_STEP_INDEX_DEFAULT) {
      Route.Step step = currentDriveOrder.getRoute().getSteps().get(0);
      Point expectedVehiclePosition
          = step.getSourcePoint() != null ? step.getSourcePoint() : step.getDestinationPoint();
      return Objects.equals(expectedVehiclePosition.getReference(), currentVehiclePosition);
    }

    Route.Step step = currentDriveOrder.getRoute().getSteps().get(routeProgressIndex);
    Point expectedVehiclePosition = step.getDestinationPoint();
    return Objects.equals(expectedVehiclePosition.getReference(), currentVehiclePosition);
  }
}
