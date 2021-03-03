/*
 * openTCS copyright information:
 * Copyright (c) 2010 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.parking;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.algorithms.Router;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * A parking strategy that tries to find parking positions that are unoccupied,
 * not on the current route of any other vehicle and as close as possible to the
 * parked vehicle's current position.
 *
 * @author Youssef Zaki (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
class OffRouteParkingStrategy
    extends AbstractParkingStrategy {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(OffRouteParkingStrategy.class.getName());

  /**
   * Creates a new OffRouteParkingPosition.
   *
   * @param kernel The system's kernel.
   * @param router A router for computing distances to parking positions.
   */
  @Inject
  public OffRouteParkingStrategy(final LocalKernel kernel,
                                 final Router router) {
    super(kernel, router);
  }

  @Override
  public Point getParkingPosition(final Vehicle vehicle) {
    Objects.requireNonNull(vehicle, "vehicle is null");
    Objects.requireNonNull(vehicle.getCurrentPosition(),
                           "vehicle's position unknown");

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
    log.info("parkingPosCandidates: " + parkingPosCandidates);
    // If none of the parking positions are usable, we can't do anything.
    if (parkingPosCandidates.isEmpty()) {
      return null;
    }
    else {
      return nearestPoint(vehicle, parkingPosCandidates);
    }
  }
}
