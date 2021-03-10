/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.parking;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import static org.opentcs.util.Assertions.checkArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A parking position supplier that tries to find the parking position with the highest priority
 * that is unoccupied, not on the current route of any other vehicle and as close as possible to the
 * vehicle's current position.
 *
 * @author Youssef Zaki (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PrioritizedParkingPositionSupplier
    extends AbstractParkingPositionSupplier {

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
   */
  @Inject
  public PrioritizedParkingPositionSupplier(InternalPlantModelService plantModelService,
                                            Router router,
                                            ParkingPositionToPriorityFunction priorityFunction) {
    super(plantModelService, router);
    this.priorityFunction = requireNonNull(priorityFunction, "priorityFunction");
  }

  @Override
  public Optional<Point> findParkingPosition(final Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    if (vehicle.getCurrentPosition() == null) {
      return Optional.empty();
    }

    int currentPriority = priorityOfCurrentPosition(vehicle);
    Set<Point> parkingPosCandidates = findUsableParkingPositions(vehicle).stream()
        .filter(point -> hasHigherPriorityThan(point, currentPriority))
        .collect(Collectors.toSet());

    if (parkingPosCandidates.isEmpty()) {
      LOG.debug("{}: No parking position candidates found.", vehicle.getName());
      return Optional.empty();
    }

    LOG.debug("{}: Selecting parking position from candidates {}.",
              vehicle.getName(),
              parkingPosCandidates);

    parkingPosCandidates = filterPositionsWithHighestPriority(parkingPosCandidates);
    Point parkingPos = nearestPoint(vehicle, parkingPosCandidates);

    LOG.debug("{}: Selected parking position {}.", vehicle.getName(), parkingPos);

    return Optional.ofNullable(parkingPos);
  }

  private int priorityOfCurrentPosition(Vehicle vehicle) {
    Point currentPos = getPlantModelService().fetchObject(Point.class,
                                                          vehicle.getCurrentPosition());
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

  private Set<Point> filterPositionsWithHighestPriority(Set<Point> positions) {
    checkArgument(!positions.isEmpty(), "'positions' must not be empty");
    
    Map<Integer, List<Point>> prioritiesToPositions = positions.stream()
        .collect(Collectors.groupingBy(point -> priorityFunction.apply(point)));

    Integer highestPriority = prioritiesToPositions.keySet().stream()
        .reduce(Integer::min)
        .get();
    
    return new HashSet<>(prioritiesToPositions.get(highestPriority));
  }
}
