/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.routing.GroupMapper;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import static org.opentcs.strategies.basic.routing.PointRouter.INFINITE_COSTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic {@link Router} implementation.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultRouter
    implements Router {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultRouter.class);
  /**
   * This class's configuration.
   */
  private final DefaultRouterConfiguration configuration;
  /**
   * The object service providing the model data.
   */
  private final TCSObjectService objectService;
  /**
   * A builder for constructing our routing tables.
   */
  private final PointRouterFactory pointRouterFactory;
  /**
   * Used to map vehicles to their routing groups.
   */
  private final GroupMapper routingGroupMapper;
  /**
   * The routes selected for each vehicle.
   */
  private final Map<Vehicle, List<DriveOrder>> routesByVehicle = new ConcurrentHashMap<>();
  /**
   * The point routers by vehicle routing group.
   */
  private final Map<String, PointRouter> pointRoutersByVehicleGroup = new ConcurrentHashMap<>();
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service providing the model data.
   * @param pointRouterFactory A factory for point routers.
   * @param routingGroupMapper Used to map vehicles to their routing groups.
   * @param configuration This class's configuration.
   */
  @Inject
  public DefaultRouter(TCSObjectService objectService,
                       PointRouterFactory pointRouterFactory,
                       GroupMapper routingGroupMapper,
                       DefaultRouterConfiguration configuration) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.pointRouterFactory = requireNonNull(pointRouterFactory, "pointRouterFactory");
    this.routingGroupMapper = requireNonNull(routingGroupMapper, "routingGroupMapper");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    synchronized (this) {
      routesByVehicle.clear();
      topologyChanged();
      initialized = true;
    }
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

    synchronized (this) {
      routesByVehicle.clear();
      pointRoutersByVehicleGroup.clear();
      initialized = false;
    }
  }

  @Override
  public void topologyChanged() {
    synchronized (this) {
      pointRoutersByVehicleGroup.clear();
    }
  }

  @Override
  public Set<Vehicle> checkRoutability(TransportOrder order) {
    requireNonNull(order, "order");

    synchronized (this) {
      Set<Vehicle> result = new HashSet<>();
      List<DriveOrder> driveOrderList = order.getFutureDriveOrders();
      DriveOrder[] driveOrders
          = driveOrderList.toArray(new DriveOrder[driveOrderList.size()]);

      // Since point routers get reset on topology changes, make sure there are point routers for 
      // all routing groups.
      createMissingPointRouters();

      for (Map.Entry<String, PointRouter> curEntry : pointRoutersByVehicleGroup.entrySet()) {
        // Get all points at the first location at which a vehicle of the current
        // type can execute the desired operation and check if an acceptable route
        // originating in one of them exists.
        for (Point curStartPoint : getDestinationPoints(driveOrders[0])) {
          if (isRoutable(curStartPoint, driveOrders, 1, curEntry.getValue())) {
            result.addAll(getVehiclesByRoutingGroup(curEntry.getKey()));
            break;
          }
        }
      }
      return result;
    }
  }

  @Override
  public Optional<List<DriveOrder>> getRoute(Vehicle vehicle,
                                             Point sourcePoint,
                                             TransportOrder transportOrder) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(sourcePoint, "sourcePoint");
    requireNonNull(transportOrder, "transportOrder");

    synchronized (this) {
      List<DriveOrder> driveOrderList = transportOrder.getFutureDriveOrders();
      DriveOrder[] driveOrders = driveOrderList.toArray(new DriveOrder[driveOrderList.size()]);
      PointRouter pointRouter = getPointRouterForVehicle(vehicle);
      OrderRouteParameterStruct params = new OrderRouteParameterStruct(driveOrders, pointRouter);
      OrderRouteResultStruct resultStruct = new OrderRouteResultStruct(driveOrderList.size());
      computeCheapestOrderRoute(sourcePoint, params, 0, resultStruct);
      return (resultStruct.bestCosts == Long.MAX_VALUE)
          ? Optional.empty()
          : Optional.of(Arrays.asList(resultStruct.bestRoute));
    }
  }

  @Override
  public Optional<Route> getRoute(Vehicle vehicle,
                                  Point sourcePoint,
                                  Point destinationPoint) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(sourcePoint, "sourcePoint");
    requireNonNull(destinationPoint, "destinationPoint");

    synchronized (this) {
      PointRouter pointRouter = getPointRouterForVehicle(vehicle);
      long costs = pointRouter.getCosts(sourcePoint, destinationPoint);
      if (costs == INFINITE_COSTS) {
        return Optional.empty();
      }
      List<Route.Step> steps = pointRouter.getRouteSteps(sourcePoint, destinationPoint);
      if (steps.isEmpty()) {
        // If the list of steps is empty, we're already at the destination point
        // Create a single step without a path.
        steps.add(new Route.Step(null, null, sourcePoint, Vehicle.Orientation.UNDEFINED, 0));
      }
      return Optional.of(new Route(steps, costs));
    }
  }

  @Override
  public long getCosts(Vehicle vehicle,
                       Point sourcePoint,
                       Point destinationPoint) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(sourcePoint, "sourcePoint");
    requireNonNull(destinationPoint, "destinationPoint");

    synchronized (this) {
      return getPointRouterForVehicle(vehicle).getCosts(sourcePoint, destinationPoint);
    }
  }

  @Override
  public long getCostsByPointRef(Vehicle vehicle,
                                 TCSObjectReference<Point> srcPointRef,
                                 TCSObjectReference<Point> dstPointRef) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(srcPointRef, "srcPointRef");
    requireNonNull(dstPointRef, "dstPointRef");

    synchronized (this) {
      return getPointRouterForVehicle(vehicle).getCosts(srcPointRef, dstPointRef);
    }
  }

  @Override
  public void selectRoute(Vehicle vehicle, List<DriveOrder> driveOrders) {
    requireNonNull(vehicle, "vehicle");

    synchronized (this) {
      if (driveOrders == null) {
        // XXX Should we remember the vehicle's current position, maybe?
        routesByVehicle.remove(vehicle);
      }
      else {
        routesByVehicle.put(vehicle, driveOrders);
      }
    }
  }

  @Override
  public Map<Vehicle, List<DriveOrder>> getSelectedRoutes() {
    synchronized (this) {
      return new HashMap<>(routesByVehicle);
    }
  }

  @Override
  public Set<Point> getTargetedPoints() {
    synchronized (this) {
      Set<Point> result = new HashSet<>();
      for (List<DriveOrder> curOrderList : routesByVehicle.values()) {
        DriveOrder finalOrder = curOrderList.get(curOrderList.size() - 1);
        result.add(finalOrder.getRoute().getFinalDestinationPoint());
      }
      return result;
    }
  }

  private void createMissingPointRouters() {
    Map<String, Vehicle> distinctRoutingGroups = new HashMap<>();
    for (Vehicle vehicle : objectService.fetchObjects(Vehicle.class)) {
      distinctRoutingGroups.putIfAbsent(routingGroupMapper.apply(vehicle), vehicle);
    }

    // Lazily create point routers if they don't exist.
    distinctRoutingGroups.forEach((routingGroup, vehicle) -> getPointRouterForVehicle(vehicle));
  }

  /**
   * Returns the {@link PointRouter} for the given vehicle considering the vehicle's routing group.
   *
   * @param vehicle The vehicle to get the point router for.
   * @return The point router.
   */
  private PointRouter getPointRouterForVehicle(Vehicle vehicle) {
    String routingGroup = routingGroupMapper.apply(vehicle);
    if (!pointRoutersByVehicleGroup.containsKey(routingGroup)) {
      pointRoutersByVehicleGroup.put(routingGroup,
                                     pointRouterFactory.createPointRouter(vehicle));
    }

    return pointRoutersByVehicleGroup.get(routingGroup);
  }

  /**
   * Checks if a route exists for a vehicle of a given type which allows the
   * vehicle to process a given list of drive orders.
   *
   * @param startPoint The point at which the route is supposed to start.
   * @param driveOrders The list of drive orders, in the order they are to be
   * processed.
   * @param nextHopIndex The index of the next drive order in the list.
   * @param pointRouter The point router to use.
   * @return <code>true</code> if, and only if, at least one route exists which
   * would allow a vehicle of the given type to process the whole list of drive
   * orders.
   */
  private boolean isRoutable(Point startPoint,
                             DriveOrder[] driveOrders,
                             int nextHopIndex,
                             PointRouter pointRouter) {
    assert startPoint != null;
    assert driveOrders != null;
    assert pointRouter != null;

    if (nextHopIndex < driveOrders.length) {
      for (Point curPoint : getDestinationPoints(driveOrders[nextHopIndex])) {
        // Check if there is a route from the starting point to the current
        // point and if the rest of the orders are routable from there, too.
        if (pointRouter.getCosts(startPoint, curPoint) != INFINITE_COSTS
            && isRoutable(curPoint, driveOrders, nextHopIndex + 1, pointRouter)) {
          // If it was possible to reach the end of the order list from here,
          // propagate the result back to the caller.
          return true;
        }
      }
      // If we haven't found an acceptable route, return false.
      return false;
    }
    // If we have reached the end of the list, it seems we have found a route.
    else {
      return true;
    }
  }

  /**
   * Compute the cheapest route along a list of drive orders/checkpoints.
   *
   * @param startPoint The current checkpoint which to start at.
   * @param params A struct describing parameters for the route to be computed.
   * @param hopIndex The current index in the list of drive orders/checkpoints.
   * @param result A struct for keeping the (partial) result in.
   */
  private void computeCheapestOrderRoute(Point startPoint,
                                         OrderRouteParameterStruct params,
                                         int hopIndex,
                                         OrderRouteResultStruct result) {
    assert startPoint != null;
    assert params != null;
    assert result != null;
    // If we haven't reached the final drive order in the list, yet...
    if (hopIndex < params.driveOrders.length) {
      // ...try every possible destination point of the current drive order as
      // the next checkpoint and recursively route from there.
      final long currentRouteCosts = result.currentCosts;
      Set<Point> destPoints = getDestinationPoints(params.driveOrders[hopIndex]);
      // If the set of destination points contains the starting point, keep only
      // that one. This is just a shortcut - it is the cheapest way to go.
      if (!configuration.routeToCurrentPosition() && destPoints.contains(startPoint)) {
        LOG.debug("Shortcutting route to {}", startPoint);
        destPoints.clear();
        destPoints.add(startPoint);
      }
      boolean routable = false;
      for (Point curDestPoint : destPoints) {
        final long hopCosts = params.pointRouter.getCosts(startPoint, curDestPoint);
        if (hopCosts == INFINITE_COSTS) {
          continue;
        }
        // Get the list of steps for the route of the current drive order.
        List<Route.Step> steps = params.pointRouter.getRouteSteps(startPoint, curDestPoint);
        if (steps.isEmpty()) {
          // If the list of steps returned is empty, we're already at the
          // destination point of the drive order - create a single step
          // without a path.
          steps = new ArrayList<>(1);
          steps.add(new Route.Step(null,
                                   null,
                                   startPoint,
                                   Vehicle.Orientation.UNDEFINED,
                                   0));
        }
        // Create a route from the list of steps gathered.
        Route hopRoute = new Route(steps, hopCosts);
        // Copy the current drive order, add the computed route to it and
        // place it in the result struct.
        DriveOrder hopOrder = params.driveOrders[hopIndex].withRoute(hopRoute);
        result.currentRoute[hopIndex] = hopOrder;
        // Calculate the costs for the route so far, too.
        result.currentCosts = currentRouteCosts + hopRoute.getCosts();
        computeCheapestOrderRoute(curDestPoint, params, hopIndex + 1, result);
        // Remember that we did find at least one route that works.
        routable = true;
      }
      if (!routable) {
        // Setting currentCosts is not strictly necessary for this algorithm,
        // but might help with debugging.
        result.currentCosts = Long.MAX_VALUE;
      }
    }
    // If we have reached the final drive order, ...
    else // If the route computed is cheaper than the best route found so far,
    // replace the latter.
    if (result.currentCosts < result.bestCosts) {
      System.arraycopy(result.currentRoute, 0, result.bestRoute, 0, result.currentRoute.length);
      result.bestCosts = result.currentCosts;
    }
  }

  /**
   * Returns all points at which a vehicle could process the given drive order.
   *
   * @param driveOrder The drive order to be processed.
   * @return A set of acceptable destination points at which a vehicle could
   * execute the given drive order's operation. If no such points exist, the
   * returned set will be empty.
   */
  private Set<Point> getDestinationPoints(DriveOrder driveOrder) {
    assert driveOrder != null;

    final DriveOrder.Destination dest = driveOrder.getDestination();
    // If the destination references a point and the operation is "just move" or
    // "park the vehicle", this is an order to send the vehicle to an explicitly
    // selected point - return an appropriate set with only that point.
    if (dest.getDestination().getReferentClass() == Point.class
        && (Destination.OP_MOVE.equals(dest.getOperation())
            || Destination.OP_PARK.equals(dest.getOperation()))) {
      // Route the vehicle to an user selected point if halting is allowed there.
      Point destPoint = objectService.fetchObject(Point.class, dest.getDestination().getName());
      requireNonNull(destPoint, "destPoint");
      final Set<Point> result = new HashSet<>();
      if (destPoint.isHaltingPosition()) {
        result.add(destPoint);
      }
      return result;
    }
    // If it's a "normal" transport order, look for destination points adjacent
    // to the destination location.
    else {
      final Set<Point> result = new HashSet<>();
      final Location destLoc = objectService.fetchObject(Location.class,
                                                         dest.getDestination().getName());
      final LocationType destLocType = objectService.fetchObject(LocationType.class,
                                                                 destLoc.getType());
      for (Location.Link curLink : destLoc.getAttachedLinks()) {
        // A link is acceptable if any of the following conditions are true:
        // - The destination operation is OP_NOP, which is allowed everywhere.
        // - The destination operation is explicitly allowed with the link.
        // - The link's set of allowed operations is empty and the destination
        //   operation is explicitly allowed with the location's type.
        // Furthermore, the point to be routed at must allow halting.
        if (Destination.OP_NOP.equals(dest.getOperation())
            || curLink.hasAllowedOperation(dest.getOperation())
            || (curLink.getAllowedOperations().isEmpty()
                && destLocType.isAllowedOperation(dest.getOperation()))) {
          Point destPoint = objectService.fetchObject(Point.class, curLink.getPoint());
          if (destPoint.isHaltingPosition()) {
            result.add(destPoint);
          }
        }
      }
      return result;
    }
  }

  /**
   * Returns all vehicles within the given routing group.
   *
   * @param routingGroup The routing group the returned vehicles should belong to.
   * @return The vehicles which have the given routing group
   */
  private Set<Vehicle> getVehiclesByRoutingGroup(String routingGroup) {
    Set<Vehicle> result = new HashSet<>();
    for (Vehicle curVehicle : objectService.fetchObjects(Vehicle.class)) {
      if (Objects.equals(routingGroupMapper.apply(curVehicle), routingGroup)) {
        result.add(curVehicle);
      }
    }
    return result;
  }

  /**
   * Contains parameters for a route to be computed.
   */
  private static final class OrderRouteParameterStruct {

    /**
     * The drive orders containing the route's checkpoints.
     */
    private final DriveOrder[] driveOrders;
    /**
     * The point router for the vehicle type.
     */
    private final PointRouter pointRouter;

    /**
     * Creates a new OrderRouteParameterStruct.
     *
     * @param driveOrders A list of drive orders to be processed as checkpoints
     * of the route to be computed.
     * @param pointRouter The point router for the vehicle type.
     */
    public OrderRouteParameterStruct(DriveOrder[] driveOrders,
                                     PointRouter pointRouter) {
      this.driveOrders = requireNonNull(driveOrders, "driveOrders");
      this.pointRouter = requireNonNull(pointRouter, "pointRouter");
    }
  }

  /**
   * A struct supporting cheapest route calculation.
   */
  private static final class OrderRouteResultStruct {

    /**
     * The (possibly partial) route currently being examined.
     */
    private DriveOrder[] currentRoute;
    /**
     * The costs of the route currently being examined.
     */
    private long currentCosts;
    /**
     * The best route found so far.
     */
    private DriveOrder[] bestRoute;
    /**
     * The costs of the best route found so far.
     */
    private long bestCosts;

    /**
     * Creates a new OrderRouteResultStruct.
     *
     * @param driveOrderCount The number of <code>DriveOrder</code>s in the
     * <code>TransportOrder</code> for which this struct is to store the
     * routing result.
     */
    public OrderRouteResultStruct(int driveOrderCount) {
      currentRoute = new DriveOrder[driveOrderCount];
      currentCosts = 0;
      bestRoute = new DriveOrder[driveOrderCount];
      bestCosts = Long.MAX_VALUE;
    }
  }
}
