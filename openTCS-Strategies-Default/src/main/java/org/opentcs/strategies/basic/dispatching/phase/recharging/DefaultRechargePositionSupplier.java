/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.recharging;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import static org.opentcs.components.kernel.Dispatcher.PROPKEY_ASSIGNED_RECHARGE_LOCATION;
import static org.opentcs.components.kernel.Dispatcher.PROPKEY_PREFERRED_RECHARGE_LOCATION;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;

/**
 * Finds assigned, preferred or (routing-wise) cheapest recharge locations for vehicles.
 */
public class DefaultRechargePositionSupplier
    implements RechargePositionSupplier {

  /**
   * The plant model service.
   */
  private final InternalPlantModelService plantModelService;
  /**
   * Our router.
   */
  private final Router router;
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param plantModelService The plant model service.
   * @param router The router to use.
   */
  @Inject
  public DefaultRechargePositionSupplier(InternalPlantModelService plantModelService,
                                         Router router) {
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
    this.router = requireNonNull(router, "router");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
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
    if (!isInitialized()) {
      return;
    }

    initialized = false;
  }

  @Override
  public List<DriveOrder.Destination> findRechargeSequence(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    if (vehicle.getCurrentPosition() == null) {
      return List.of();
    }

    Map<Location, Set<Point>> rechargeLocations
        = findLocationsForOperation(vehicle.getRechargeOperation(),
                                    vehicle,
                                    router.getTargetedPoints());

    String assignedRechargeLocationName = vehicle.getProperty(PROPKEY_ASSIGNED_RECHARGE_LOCATION);
    if (assignedRechargeLocationName != null) {
      Location location = pickLocationWithName(assignedRechargeLocationName,
                                               rechargeLocations.keySet());
      if (location == null) {
        return List.of();
      }
      // XXX We should check whether there actually is a viable route to the location.
      return List.of(createDestination(location, vehicle.getRechargeOperation()));
    }

    // XXX We should check whether there actually is a viable route to the chosen location.
    return Optional.ofNullable(vehicle.getProperty(PROPKEY_PREFERRED_RECHARGE_LOCATION))
        .map(name -> pickLocationWithName(name, rechargeLocations.keySet()))
        .or(() -> Optional.ofNullable(findCheapestLocation(rechargeLocations, vehicle)))
        .map(location -> List.of(createDestination(location, vehicle.getRechargeOperation())))
        .orElse(List.of());
  }

  @Nullable
  private Location findCheapestLocation(Map<Location, Set<Point>> locations, Vehicle vehicle) {
    Point curPos = plantModelService.fetchObject(Point.class, vehicle.getCurrentPosition());

    return locations.entrySet().stream()
        .map(entry -> bestAccessPointCandidate(vehicle, curPos, entry.getKey(), entry.getValue()))
        .filter(candidate -> candidate.isPresent())
        .map(candidate -> candidate.get())
        .min(Comparator.comparingLong(candidate -> candidate.costs))
        .map(candidate -> candidate.location)
        .orElse(null);
  }

  private DriveOrder.Destination createDestination(Location location, String operation) {
    return new DriveOrder.Destination(location.getReference())
        .withOperation(operation);
  }

  @Nullable
  private Location pickLocationWithName(String name, Set<Location> locations) {
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

    for (Location curLoc : plantModelService.fetchObjects(Location.class)) {
      LocationType lType = plantModelService.fetchObject(LocationType.class, curLoc.getType());
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
    return location.getAttachedLinks().stream()
        .filter(link -> allowsOperation(link, rechargeOp))
        .map(link -> plantModelService.fetchObject(Point.class, link.getPoint()))
        .filter(accessPoint -> isPointUnoccupiedFor(accessPoint, vehicle, targetedPoints))
        .collect(Collectors.toSet());
  }

  /**
   * Checks if the given link either does not define any allowed operations at all (meaning it does
   * not override the allowed operations of the corresponding location's location type), or - if it
   * does - explicitly allows the required recharge operation.
   *
   * @param link The link to be checked.
   * @param operation The operation to be checked for.
   * @return <code>true</code> if, and only if, the given link does not disallow the given
   * operation.
   */
  private boolean allowsOperation(Location.Link link, String operation) {
    // This link is only interesting if it either does not define any allowed operations (does
    // not override the allowed operations of the corresponding location) at all or, if it does,
    // allows the required recharge operation.
    return link.getAllowedOperations().isEmpty() || link.hasAllowedOperation(operation);
  }

  private Optional<LocationCandidate> bestAccessPointCandidate(Vehicle vehicle,
                                                               Point srcPosition,
                                                               Location location,
                                                               Set<Point> destPositions) {
    return destPositions.stream()
        .map(point -> new LocationCandidate(location,
                                            router.getCosts(vehicle,
                                                            srcPosition,
                                                            point,
                                                            Set.of())))
        .min(Comparator.comparingLong(candidate -> candidate.costs));
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
        .noneMatch(point -> pointOccupiedOrTargetedByOtherVehicle(point,
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

  /**
   * Gathers a set of all points from all blocks that the given point is a member of.
   *
   * @param point The point to check.
   * @return A set of all points from all blocks that the given point is a member of.
   */
  private Set<Point> expandPoints(Point point) {
    return plantModelService.expandResources(Set.of(point.getReference())).stream()
        .filter(resource -> Point.class.equals(resource.getReference().getReferentClass()))
        .map(resource -> (Point) resource)
        .collect(Collectors.toSet());
  }

  private static class LocationCandidate {

    private final Location location;
    private final long costs;

    LocationCandidate(Location location, long costs) {
      this.location = location;
      this.costs = costs;
    }
  }
}
