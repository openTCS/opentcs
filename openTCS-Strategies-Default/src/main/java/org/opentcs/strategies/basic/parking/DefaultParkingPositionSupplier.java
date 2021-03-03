/*
 * openTCS copyright information:
 * Copyright (c) 2010 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.parking;

import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.kernel.Router;
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
   * @param kernel The system's kernel.
   * @param router A router for computing distances to parking positions.
   */
  @Inject
  public DefaultParkingPositionSupplier(final LocalKernel kernel,
                                        final Router router) {
    super(kernel, router);
  }

  @Override
  public Optional<Point> findParkingPosition(final Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(vehicle.getCurrentPosition(), "vehicle.getCurrentPosition()");

    // Find out which points are destination points of the current routes of
    // all vehicles, and keep them. (Multiple lookups ahead.)
    Set<Point> targetedPoints = getRouter().getTargetedPoints();

    Set<Point> parkingPosCandidates = new HashSet<>();
    for (Map.Entry<Point, Set<Point>> curEntry : getParkingPositions().entrySet()) {
      // Check if all points that are required to use the parking position are
      // free (or occupied by the same vehicle that is to be parked) and not
      // targeted by another vehicle.
      boolean usable = true;
      for (Point blockPoint : curEntry.getValue()) {
        // Get an up-to-date copy of the point from the kernel first.
        Point blockPointActu = getKernel().getTCSObject(Point.class,
                                                        blockPoint.getReference());
        // If the point is occupied by another vehicle, give up.
        if (blockPointActu.getOccupyingVehicle() != null
            && !blockPointActu.getOccupyingVehicle().equals(vehicle.getReference())) {
          usable = false;
          break;
        }
        // If the point is the destination of another vehicle, give up.
        else if (targetedPoints.contains(blockPointActu)) {
          usable = false;
          break;
        }
      }
      // If there's nothing keeping us from using the point, keep.
      if (usable) {
        parkingPosCandidates.add(curEntry.getKey());
      }
    }

    if (parkingPosCandidates.isEmpty()) {
      LOG.debug("No parking position candidates found.");
      return Optional.empty();
    }
    else {
      Point nearestPoint = nearestPoint(vehicle, parkingPosCandidates);
      LOG.debug("Selected parking position {} for vehicle {} from candidates {}.",
                nearestPoint,
                vehicle.getName(),
                parkingPosCandidates);
      return Optional.ofNullable(nearestPoint(vehicle, parkingPosCandidates));
    }
  }
}
