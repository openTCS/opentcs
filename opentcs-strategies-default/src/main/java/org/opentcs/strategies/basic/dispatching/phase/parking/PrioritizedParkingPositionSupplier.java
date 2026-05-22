// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.parking;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
 * A parking position supplier that tries to find the parking position with the highest priority
 * that is unoccupied, not on the current route of any other vehicle and as close as possible to the
 * vehicle's current position.
 */
public class PrioritizedParkingPositionSupplier
    extends
      AbstractParkingPositionSupplier {

  /**
   * This class's Logger.
   */
  private static final Logger LOG
      = LoggerFactory.getLogger(PrioritizedParkingPositionSupplier.class);
  /**
   * A function computing the priority of a parking position.
   */
  private final ParkingPositionToPriorityFunction priorityFunction;

  /**
   * Creates a new instance.
   *
   * @param plantModelService The plant model service.
   * @param router A router for computing travel costs to parking positions.
   * @param priorityFunction A function computing the priority of a parking position.
   * @param targetedPointsSupplier Returns all points which are currently targeted by vehicles.
   * @param configuration The dispatcher configuration.
   * @param routeSelector Selects a route from a set of routes.
   */
  @Inject
  public PrioritizedParkingPositionSupplier(
      InternalPlantModelService plantModelService,
      Router router,
      ParkingPositionToPriorityFunction priorityFunction,
      TargetedPointsSupplier targetedPointsSupplier,
      DefaultDispatcherConfiguration configuration,
      RouteSelector routeSelector
  ) {
    super(plantModelService, router, targetedPointsSupplier, configuration, routeSelector);
    this.priorityFunction = requireNonNull(priorityFunction, "priorityFunction");
  }

  @Override
  public Optional<Point> findParkingPosition(final Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    if (vehicle.getCurrentPosition() == null) {
      return Optional.empty();
    }

    int currentPriority = priorityOfCurrentPosition(vehicle);
    Map<Integer, List<Point>> parkingPosCandidatesByPriority
        = findUsableParkingPositions(vehicle).stream()
            .filter(point -> hasHigherPriorityThan(point, currentPriority))
            .collect(Collectors.groupingBy(priorityFunction));

    if (parkingPosCandidatesByPriority.isEmpty()) {
      LOG.debug("{}: No parking position candidates found.", vehicle.getName());
      return Optional.empty();
    }

    LOG.debug(
        "{}: Selecting parking position from candidates {}.",
        vehicle.getName(),
        parkingPosCandidatesByPriority
    );

    Point parkingPos = parkingPosCandidatesByPriority.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(entry -> nearestPoint(vehicle, entry.getValue()))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);

    LOG.debug("{}: Selected parking position {}.", vehicle.getName(), parkingPos);

    return Optional.ofNullable(parkingPos);
  }

  private int priorityOfCurrentPosition(Vehicle vehicle) {
    Point currentPos = getPlantModelService().fetch(
        Point.class,
        vehicle.getCurrentPosition()
    ).orElseThrow();
    return priorityFunction
        .andThen(priority -> priority != null ? priority : Integer.MAX_VALUE)
        .apply(currentPos);
  }

  private boolean hasHigherPriorityThan(Point point, Integer priority) {
    Integer pointPriority = priorityFunction.apply(point);
    if (pointPriority == null) {
      return false;
    }

    return pointPriority < priority;
  }
}
