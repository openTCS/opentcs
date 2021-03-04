/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.parking;

import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * An abstract base class for parking position suppliers.
 *
 * @author Youssef Zaki (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class AbstractParkingPositionSupplier
    implements ParkingPositionSupplier {

  /**
   * The object service.
   */
  private final TCSObjectService objectService;
  /**
   * A router for computing distances to parking positions.
   */
  private final Router router;
  /**
   * A map containing all points in the model that are parking positions as keys
   * and sets of all points sharing the same block(s) as values.
   */
  private final Map<Point, Set<Point>> parkingPositions = new ConcurrentHashMap<>();
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param objectService The system's kernel.
   * @param router A router for computing distances to parking positions.
   */
  protected AbstractParkingPositionSupplier(final TCSObjectService objectService,
                                            final Router router) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.router = requireNonNull(router, "router");
  }

  @Override
  public void initialize() {
    if (initialized) {
      return;
    }

    Set<Block> allBlocks = objectService.fetchObjects(Block.class);
    // Get all parking positions from the kernel.
    for (Point curPoint : objectService.fetchObjects(Point.class)) {
      if (curPoint.isParkingPosition()) {
        // Gather all points from all blocks that this point is a member of.
        parkingPositions.put(curPoint, getBlockedPoints(curPoint, allBlocks));
      }
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

    parkingPositions.clear();

    initialized = false;
  }

  /**
   * Returns the object service.
   *
   * @return The object service.
   */
  public TCSObjectService getObjectService() {
    return objectService;
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
   * Returns a map containing all points in the model that are parking
   * positions as keys and sets of all points sharing the same block(s) as
   * values.
   *
   * @return A map containing all points in the model that are parking
   * positions and their corresponding blocked points.
   */
  protected final Map<Point, Set<Point>> getParkingPositions() {
    return parkingPositions;
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

    Point vehiclePos = objectService.fetchObject(Point.class, vehicle.getCurrentPosition());

    long lowestCost = Long.MAX_VALUE;
    Point nearestPoint = null;
    for (Point curPoint : points) {
      long curCost = router.getCosts(vehicle, vehiclePos, curPoint);
      if (curCost < lowestCost) {
        nearestPoint = curPoint;
        lowestCost = curCost;
      }
    }
    return nearestPoint;
  }

  /**
   * Gathers a set of all points from all given blocks that the given point is a
   * member of.
   *
   * @param point The parking position to check.
   * @param blocks The blocks to scan for the parking position.
   * @return A set of all points from all given blocks that the given point is a
   * member of.
   */
  @SuppressWarnings("unchecked")
  private Set<Point> getBlockedPoints(Point point, Set<Block> blocks) {
    requireNonNull(point, "point");
    requireNonNull(blocks, "blocks");

    Set<Point> result = new HashSet<>();

    // The parking position itself is always required.
    result.add(point);

    // Check for every block if the given point is part of it.
    for (Block curBlock : blocks) {
      if (curBlock.getMembers().contains(point.getReference())) {
        // Check for every member of the block if it's a point. If it is, add it
        // to the resulting set.
        for (TCSObjectReference<?> memberRef : curBlock.getMembers()) {
          if (Point.class.equals(memberRef.getReferentClass())) {
            result.add(objectService.fetchObject(Point.class,
                                                 (TCSObjectReference<Point>) memberRef));
          }
        }
      }
    }
    return result;
  }
}
