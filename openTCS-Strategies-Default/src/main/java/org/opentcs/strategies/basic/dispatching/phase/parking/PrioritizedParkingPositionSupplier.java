/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.parking;

import java.util.HashSet;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A parking position supplier that tries to find the parking position with the highest priority
 * that is unoccupied, not on the current route of any other vehicle and as close as possible to the
 * parked vehicle's current position.
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
   * Compares parking positions by their priorities.
   */
  private final ParkingPositionPriorityComparator priorityComparator;

  /**
   * Creates a new instance.
   *
   * @param plantModelService The plant model service.
   * @param router A router for computing travel costs to parking positions.
   * @param priorityFunction A function computing the priority of a parking position.
   * @param priorityComparator Compares parking positions by their priorities.
   */
  @Inject
  public PrioritizedParkingPositionSupplier(InternalPlantModelService plantModelService,
                                            Router router,
                                            ParkingPositionToPriorityFunction priorityFunction,
                                            ParkingPositionPriorityComparator priorityComparator) {
    super(plantModelService, router);
    this.priorityFunction = requireNonNull(priorityFunction, "priorityFunction");
    this.priorityComparator = requireNonNull(priorityComparator, "priorityComparator");
  }

  @Override
  public Optional<Point> findParkingPosition(final Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    if (vehicle.getCurrentPosition() == null) {
      return Optional.empty();
    }

    Set<Point> parkingPosCandidates = findUsableParkingPositions(vehicle).stream()
        .filter(point -> priorityFunction.apply(point) != null)
        .collect(Collectors.toSet());

    if (parkingPosCandidates.isEmpty()) {
      LOG.debug("{}: No parking position candidates found.", vehicle.getName());
      return Optional.empty();
    }

    Point parkingPos = parkingPosCandidates.stream()
        .sorted(priorityComparator)
        .findFirst()
        .get();

    if (Objects.equals(vehicle.getCurrentPosition(), parkingPos.getReference())) {
      LOG.debug("{}: Already at the best parking position available.", vehicle.getName());
      return Optional.empty();
    }

    LOG.debug("{}: Selected parking position {} from candidates {}.",
              vehicle.getName(),
              parkingPos,
              parkingPosCandidates);
    return Optional.ofNullable(parkingPos);
  }

  private Set<Point> findUsableParkingPositions(Vehicle vehicle) {
    // Find out which points are destination points of the current routes of
    // all vehicles, and keep them. (Multiple lookups ahead.)
    Set<Point> targetedPoints = getRouter().getTargetedPoints();

    Set<Point> parkingPosCandidates = new HashSet<>();
    for (Point parkingPos : fetchAllParkingPositions()) {
      // Check if all points that are required to use the parking position are
      // free (or occupied by the same vehicle that is to be parked) and not
      // targeted by another vehicle.
      boolean usable = true;
      for (Point blockPoint : expandPoints(parkingPos)) {
        // If the point is occupied by another vehicle, give up.
        if (blockPoint.getOccupyingVehicle() != null
            && !blockPoint.getOccupyingVehicle().equals(vehicle.getReference())) {
          usable = false;
          break;
        }
        // If the point is the destination of another vehicle, give up.
        else if (targetedPoints.contains(blockPoint)) {
          usable = false;
          break;
        }
      }
      // If there's nothing keeping us from using the point, keep.
      if (usable) {
        parkingPosCandidates.add(parkingPos);
      }
    }
    return parkingPosCandidates;
  }
}
