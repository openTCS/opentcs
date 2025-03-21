// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.parking;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.components.kernel.RouteSelector;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.phase.TargetedPointsSupplier;

/**
 * An abstract base class for parking position suppliers.
 */
public abstract class AbstractParkingPositionSupplier
    implements
      ParkingPositionSupplier {

  /**
   * The plant model service.
   */
  private final InternalPlantModelService plantModelService;
  /**
   * A router for computing distances to parking positions.
   */
  private final Router router;
  /**
   * Finds all points which are currently targeted by vehicles.
   */
  private final TargetedPointsSupplier targetedPointsSupplier;
  /**
   * The dispatcher configuration.
   */
  private final DefaultDispatcherConfiguration configuration;
  /**
   * Selects a route from a set of routes.
   */
  private final RouteSelector routeSelector;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param plantModelService The plant model service.
   * @param router A router for computing distances to parking positions.
   * @param targetedPointsSupplier Finds all points which are currently targeted by vehicles.
   * @param configuration The dispatcher configuraton.
   * @param routeSelector Selects a route from a set of routes.
   */
  protected AbstractParkingPositionSupplier(
      InternalPlantModelService plantModelService,
      Router router,
      TargetedPointsSupplier targetedPointsSupplier,
      DefaultDispatcherConfiguration configuration,
      RouteSelector routeSelector
  ) {
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
    this.router = requireNonNull(router, "router");
    this.targetedPointsSupplier = requireNonNull(targetedPointsSupplier, "targetedPointsSupplier");
    this.configuration = requireNonNull(configuration, "configuration");
    this.routeSelector = requireNonNull(routeSelector, "routeSelector");
  }

  @Override
  public void initialize() {
    if (initialized) {
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
    if (!initialized) {
      return;
    }

    initialized = false;
  }

  /**
   * Returns the plant model service.
   *
   * @return The plant model service.
   */
  public InternalPlantModelService getPlantModelService() {
    return plantModelService;
  }

  /**
   * Returns the system's router.
   *
   * @return The system's router.
   */
  public Router getRouter() {
    return router;
  }

  /**
   * Returns a set of parking positions usable for the given vehicle (usable in the sense that these
   * positions are not occupied by other vehicles).
   *
   * @param vehicle The vehicles to find parking positions for.
   * @return The set of usable parking positions.
   */
  protected Set<Point> findUsableParkingPositions(Vehicle vehicle) {
    return fetchAllParkingPositions().stream()
        .filter(
            point -> isPointUnoccupiedFor(
                point, vehicle, targetedPointsSupplier.getTargetedPoints()
            )
        )
        .collect(Collectors.toSet());
  }

  /**
   * Returns from the given set of points the one that is nearest to the given
   * vehicle.
   *
   * @param vehicle The vehicle.
   * @param points The set of points to select the nearest one from.
   * @return The point nearest to the given vehicle.
   */
  @Nullable
  protected Point nearestPoint(Vehicle vehicle, Set<Point> points) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(points, "points");

    if (vehicle.getCurrentPosition() == null) {
      return null;
    }

    Point vehiclePos = plantModelService.fetchObject(Point.class, vehicle.getCurrentPosition());

    return points.stream()
        .map(point -> parkingPositionCandidate(vehicle, vehiclePos, point))
        .filter(candidate -> candidate.costs < Long.MAX_VALUE)
        .min(Comparator.comparingLong(candidate -> candidate.costs))
        .map(candidate -> candidate.point)
        .orElse(null);
  }

  /**
   * Gathers a set of all points from all blocks that the given point is a member of.
   *
   * @param point The point to check.
   * @return A set of all points from all blocks that the given point is a member of.
   */
  protected Set<Point> expandPoints(Point point) {
    return plantModelService.expandResources(Collections.singleton(point.getReference())).stream()
        .filter(resource -> Point.class.equals(resource.getReference().getReferentClass()))
        .map(resource -> (Point) resource)
        .collect(Collectors.toSet());
  }

  protected Set<Point> fetchAllParkingPositions() {
    return plantModelService.fetchObjects(Point.class, point -> point.isParkingPosition());
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
  private boolean isPointUnoccupiedFor(
      Point accessPoint,
      Vehicle vehicle,
      Set<Point> targetedPoints
  ) {
    return expandPoints(accessPoint).stream()
        .allMatch(
            point -> !pointOccupiedOrTargetedByOtherVehicle(
                point,
                vehicle,
                targetedPoints
            )
        );
  }

  private boolean pointOccupiedOrTargetedByOtherVehicle(
      Point pointToCheck,
      Vehicle vehicle,
      Set<Point> targetedPoints
  ) {
    if (pointToCheck.getOccupyingVehicle() != null
        && !pointToCheck.getOccupyingVehicle().equals(vehicle.getReference())) {
      return true;
    }
    else if (targetedPoints.contains(pointToCheck)) {
      return true;
    }
    return false;
  }

  private PointCandidate parkingPositionCandidate(
      Vehicle vehicle,
      Point srcPosition,
      Point destPosition
  ) {
    return new PointCandidate(
        destPosition,
        routeSelector
            .select(
                router.getRoutes(
                    vehicle,
                    srcPosition,
                    destPosition,
                    Set.of(),
                    configuration.maxRoutesToConsider()
                )
            )
            .map(route -> route.getCosts())
            .orElse(Long.MAX_VALUE)
    );
  }

  private static class PointCandidate {

    private final Point point;
    private final long costs;

    PointCandidate(Point point, long costs) {
      this.point = point;
      this.costs = costs;
    }
  }
}
