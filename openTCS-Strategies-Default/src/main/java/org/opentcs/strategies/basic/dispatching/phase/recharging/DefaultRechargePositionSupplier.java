/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.recharging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import javax.inject.Inject;
import static org.opentcs.components.kernel.Dispatcher.PROPKEY_ASSIGNED_RECHARGE_LOCATION;
import static org.opentcs.components.kernel.Dispatcher.PROPKEY_PREFERRED_RECHARGE_LOCATION;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;

/**
 * Finds assigned, preferred or (routing-wise) cheapest recharge locations for vehicles.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultRechargePositionSupplier
    implements RechargePositionSupplier {

  /**
   * The object service.
   */
  private final TCSObjectService objectService;
  /**
   * Our router.
   */
  private final Router router;
  /**
   * Maps locations' access points to points sharing the same block(s).
   */
  private final Map<Point, Set<Point>> accessPoints = new ConcurrentHashMap<>();
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service.
   * @param router The router to use.
   */
  @Inject
  public DefaultRechargePositionSupplier(final TCSObjectService objectService,
                                         final Router router) {
    this.objectService = requireNonNull(objectService, "kernel");
    this.router = requireNonNull(router, "router");
  }

  @Override
  public void initialize() {
    if (initialized) {
      return;
    }
    accessPoints.clear();
    Set<Block> allBlocks = objectService.fetchObjects(Block.class);
    // Get all locations from the kernel, and for each of their access points,
    // build a set of points that share the same block(s).
    for (Location curLoc : objectService.fetchObjects(Location.class)) {
      for (Point accessPoint : findAccessPoints(curLoc)) {
        accessPoints.put(accessPoint, getBlockedPoints(accessPoint, allBlocks));
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
    accessPoints.clear();
    initialized = false;
  }

  @Override
  public List<DriveOrder.Destination> findRechargeSequence(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    if (vehicle.getCurrentPosition() == null) {
      return new ArrayList<>();
    }

    Map<Location, Set<Point>> rechargeLocations
        = findLocationsForOperation(vehicle.getRechargeOperation(),
                                    vehicle,
                                    router.getTargetedPoints());

    String assignedRechargeLocationName = vehicle.getProperty(PROPKEY_ASSIGNED_RECHARGE_LOCATION);
    if (assignedRechargeLocationName != null) {
      Location location = selectLocationWithName(assignedRechargeLocationName,
                                                 rechargeLocations.keySet());
      if (location == null) {
        return new ArrayList<>();
      }
      // XXX Strictly, we should check whether there is a viable route to the location.
      return Arrays.asList(createDestination(location, vehicle.getRechargeOperation()));
    }

    String preferredRechargeLocationName = vehicle.getProperty(PROPKEY_PREFERRED_RECHARGE_LOCATION);
    if (assignedRechargeLocationName != null) {
      Location location = selectLocationWithName(preferredRechargeLocationName,
                                                 rechargeLocations.keySet());
      if (location != null) {
        // XXX Strictly, we should check whether there is a viable route to the location.
        return Arrays.asList(createDestination(location, vehicle.getRechargeOperation()));
      }
    }

    Location bestLocation = findCheapestLocation(rechargeLocations, vehicle);
    if (bestLocation != null) {
      return Arrays.asList(createDestination(bestLocation, vehicle.getRechargeOperation()));
    }

    return new ArrayList<>();
  }

  @Nullable
  private Location findCheapestLocation(Map<Location, Set<Point>> locations, Vehicle vehicle) {
    Point curPos = objectService.fetchObject(Point.class, vehicle.getCurrentPosition());

    Location cheapestLocation = null;
    long cheapestCosts = Long.MAX_VALUE;
    for (Map.Entry<Location, Set<Point>> entry : locations.entrySet()) {
      long costs = getMinimumAccessPointCosts(vehicle, curPos, entry.getValue());

      if (costs < cheapestCosts) {
        cheapestCosts = costs;
        cheapestLocation = entry.getKey();
      }
    }

    return cheapestLocation;
  }

  private DriveOrder.Destination createDestination(Location location, String operation) {
    return new DriveOrder.Destination(location.getReference())
        .withOperation(operation);
  }

  @Nullable
  private Location selectLocationWithName(String name, Set<Location> locations) {
    return locations.stream()
        .filter(location -> name.equals(location.getName()))
        .findAny()
        .orElse(null);
  }

  /**
   * Finds locations allowing the given operation, and the points they would be accessible from for
   * the given vehicle.
   *
   * @param operation The operation.
   * @param vehicle The vehicle.
   * @param targetedPoints The points that are currently targeted by vehicles.
   * @return The locations allowing the given operation, and the points they would be accessible
   * from.
   */
  private Map<Location, Set<Point>> findLocationsForOperation(String operation,
                                                              Vehicle vehicle,
                                                              Set<Point> targetedPoints) {
    Map<Location, Set<Point>> result = new HashMap<>();

    for (Location curLoc : objectService.fetchObjects(Location.class)) {
      LocationType lType = objectService.fetchObject(LocationType.class, curLoc.getType());
      if (lType.isAllowedOperation(operation)) {
        Set<Point> points = findUnoccupiedAccessPointsForOperation(curLoc,
                                                                   operation,
                                                                   vehicle,
                                                                   targetedPoints);
        if (!points.isEmpty()) {
          result.put(curLoc, points);
        }
      }
    }

    return result;
  }

  private Set<Point> findUnoccupiedAccessPointsForOperation(Location location,
                                                            String rechargeOp,
                                                            Vehicle vehicle,
                                                            Set<Point> targetedPoints) {
    Set<Point> result = new HashSet<>();

    for (Location.Link curLink : location.getAttachedLinks()) {
      // This link is only interesting if it either does not define any allowed operations at all
      // or, if it does, allows the required recharge operation.
      if (curLink.getAllowedOperations().isEmpty() || curLink.hasAllowedOperation(rechargeOp)) {
        Point accessPoint = objectService.fetchObject(Point.class, curLink.getPoint());

        if (isPointUnoccupiedFor(accessPoint, vehicle, targetedPoints)) {
          result.add(accessPoint);
        }
      }
    }

    return result;
  }

  private long getMinimumAccessPointCosts(Vehicle vehicle,
                                          Point srcPosition,
                                          Set<Point> destPositions) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(srcPosition, "srcPosition");

    long bestLinkCosts = Long.MAX_VALUE;
    for (Point destPosition : destPositions) {
      long linkCosts = router.getCostsByPointRef(vehicle,
                                                 srcPosition.getReference(),
                                                 destPosition.getReference());
      bestLinkCosts = Math.min(linkCosts, bestLinkCosts);
    }
    return bestLinkCosts;
  }

  /**
   * Checks whether the given point is a potential, unoccupied target position for the given
   * vehicle.
   *
   * @param accessPoint The point to be checked.
   * @param vehicle The vehicle to be checked for.
   * @param targetedPoints All currently known targeted points.
   * @return <code>true</code> if, and only if, the given point is a potential and unoccupied target
   * position for the given vehicle.
   */
  private boolean isPointUnoccupiedFor(Point accessPoint,
                                       Vehicle vehicle,
                                       Set<Point> targetedPoints) {
    for (Point blockedPoint : accessPoints.get(accessPoint)) {
      Point blockedPointActu = objectService.fetchObject(Point.class, blockedPoint.getReference());
      // If the point is occupied by another vehicle, give up this link.
      if (blockedPointActu.getOccupyingVehicle() != null
          && !blockedPointActu.getOccupyingVehicle().equals(vehicle.getReference())) {
        return false;
      }
      // If the point is targeted by another vehicle, give up this link.
      else if (targetedPoints.contains(blockedPointActu)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Gathers a set of all points from all given blocks that the given point is a member of.
   *
   * @param point The point to check.
   * @param blocks The blocks to scan for the point.
   * @return A set of all points from all given blocks that the given point is a
   * member of.
   */
  @SuppressWarnings("unchecked")
  private Set<Point> getBlockedPoints(Point point, Set<Block> blocks) {
    requireNonNull(point, "point");
    requireNonNull(blocks, "blocks");

    Set<Point> result = new HashSet<>();

    // The point itself is always required.
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

  /**
   * Returns a set of points from which the given location can be accessed.
   *
   * @param location The location.
   * @return A set of points from which the given location can be accessed.
   */
  private Set<Point> findAccessPoints(Location location) {
    requireNonNull(location, "location");

    Set<Point> result = new HashSet<>();

    for (Location.Link curLink : location.getAttachedLinks()) {
      result.add(objectService.fetchObject(Point.class, curLink.getPoint()));
    }

    return result;
  }
}
