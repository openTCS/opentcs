// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.parking;

import static java.util.Objects.requireNonNull;
import static org.opentcs.components.kernel.Dispatcher.PROPKEY_ASSIGNED_PARKING_POSITION;
import static org.opentcs.components.kernel.Dispatcher.PROPKEY_PREFERRED_PARKING_POSITION;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.Set;
import org.opentcs.components.kernel.RouteSelector;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.phase.TargetedPointsSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A parking position supplier that tries to find parking positions that are unoccupied,
 * not on the current route of any other vehicle and as close as possible to the
 * parked vehicle's current position.
 */
public class DefaultParkingPositionSupplier
    extends
      AbstractParkingPositionSupplier {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultParkingPositionSupplier.class);

  /**
   * Creates a new instance.
   *
   * @param plantModelService The plant model service.
   * @param router A router for computing travel costs to parking positions.
   * @param targetedPointsSupplier Finds all points which are currently targeted by vehicles.
   * @param configuration The dispatcher configuration.
   * @param routeSelector Selects a route from a set of routes.
   */
  @Inject
  public DefaultParkingPositionSupplier(
      InternalPlantModelService plantModelService,
      Router router,
      TargetedPointsSupplier targetedPointsSupplier,
      DefaultDispatcherConfiguration configuration,
      RouteSelector routeSelector
  ) {
    super(plantModelService, router, targetedPointsSupplier, configuration, routeSelector);
  }

  @Override
  public Optional<Point> findParkingPosition(final Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    if (vehicle.getCurrentPosition() == null) {
      return Optional.empty();
    }

    Set<Point> parkingPosCandidates = findUsableParkingPositions(vehicle);

    if (parkingPosCandidates.isEmpty()) {
      LOG.debug("No parking position candidates found.");
      return Optional.empty();
    }

    // Check if the vehicle has an assigned parking position.
    // If yes, return either that (if it's with the available points) or none.
    String assignedParkingPosName = vehicle.getProperty(PROPKEY_ASSIGNED_PARKING_POSITION);
    if (assignedParkingPosName != null) {
      return Optional.ofNullable(pickPointWithName(assignedParkingPosName, parkingPosCandidates));
    }

    // Check if the vehicle has a preferred parking position.
    // If yes, and if it's with the available points, return that.
    String preferredParkingPosName = vehicle.getProperty(PROPKEY_PREFERRED_PARKING_POSITION);
    if (preferredParkingPosName != null) {
      Point preferredPoint = pickPointWithName(preferredParkingPosName, parkingPosCandidates);
      if (preferredPoint != null) {
        return Optional.of(preferredPoint);
      }
    }

    Point nearestPoint = nearestPoint(vehicle, parkingPosCandidates);
    LOG.debug(
        "Selected parking position {} for vehicle {} from candidates {}.",
        nearestPoint,
        vehicle.getName(),
        parkingPosCandidates
    );
    return Optional.ofNullable(nearestPoint);
  }

  @Nullable
  private Point pickPointWithName(String name, Set<Point> points) {
    return points.stream()
        .filter(point -> name.equals(point.getName()))
        .findAny()
        .orElse(null);
  }
}
