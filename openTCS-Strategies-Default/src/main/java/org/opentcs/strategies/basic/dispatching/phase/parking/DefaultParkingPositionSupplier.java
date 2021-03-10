/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.parking;

import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import static org.opentcs.components.kernel.Dispatcher.PROPKEY_ASSIGNED_PARKING_POSITION;
import static org.opentcs.components.kernel.Dispatcher.PROPKEY_PREFERRED_PARKING_POSITION;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A parking position supplier that tries to find parking positions that are unoccupied,
 * not on the current route of any other vehicle and as close as possible to the
 * parked vehicle's current position.
 *
 * @author Youssef Zaki (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultParkingPositionSupplier
    extends AbstractParkingPositionSupplier {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultParkingPositionSupplier.class);

  /**
   * Creates a new instance.
   *
   * @param plantModelService The plant model service.
   * @param router A router for computing travel costs to parking positions.
   */
  @Inject
  public DefaultParkingPositionSupplier(InternalPlantModelService plantModelService,
                                        Router router) {
    super(plantModelService, router);
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
    LOG.debug("Selected parking position {} for vehicle {} from candidates {}.",
              nearestPoint,
              vehicle.getName(),
              parkingPosCandidates);
    return Optional.ofNullable(nearestPoint);
  }

  @Nullable
  private Point pickPointWithName(String name, Set<Point> points) {
    return points.stream()
        .filter(point -> name.equals(point.getName()))
        .findAny()
        .orElse(null);
  }

  private Set<Point> findUsableParkingPositions(Vehicle vehicle) {
    // Find out which points are destination points of the current routes of
    // all vehicles, and keep them. (Multiple lookups ahead.)
    Set<Point> targetedPoints = getRouter().getTargetedPoints();

    return fetchAllParkingPositions().stream()
        .filter(point -> isPointUnoccupiedFor(point, vehicle, targetedPoints))
        .collect(Collectors.toSet());
  }

  /**
   * Checks if ALL points within the same block as the given access point are NOT occupied or
   * targeted by any other vehicle than the given one.
   *
   * @param accessPoint The point to be checked.
   * @param vehicle The vehicle to be checked for.
   * @param targetedPoints All currently known targeted points.
   * @return <code>true</code> if, and only if, ALL points within the same block as the given access
   * point are NOT occupied or targeted by any other vehicle than the given one.
   */
  private boolean isPointUnoccupiedFor(Point accessPoint,
                                       Vehicle vehicle,
                                       Set<Point> targetedPoints) {
    return expandPoints(accessPoint).stream()
        .allMatch(point -> !pointOccupiedOrTargetedByOtherVehicle(point,
                                                                  vehicle,
                                                                  targetedPoints));
  }

  private boolean pointOccupiedOrTargetedByOtherVehicle(Point pointToCheck,
                                                        Vehicle vehicle,
                                                        Set<Point> targetedPoints) {
    if (pointToCheck.getOccupyingVehicle() != null
        && !pointToCheck.getOccupyingVehicle().equals(vehicle.getReference())) {
      return true;
    }
    else if (targetedPoints.contains(pointToCheck)) {
      return true;
    }
    return false;
  }
}
