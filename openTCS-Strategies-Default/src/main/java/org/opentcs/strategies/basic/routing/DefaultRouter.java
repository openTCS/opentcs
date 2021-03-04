/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import com.google.inject.BindingAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Location.Link;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import static org.opentcs.strategies.basic.routing.RoutingTable.INFINITE_COSTS;
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
   * The key of a vehicle property defining the group of vehicles that sharing the same routing
   * table.
   * <p>
   * The value is expected to be an integer, the default value is {@link #DEFAULT_ROUTING_GROUP}.
   * </p>
   */
  public static final String PROPKEY_ROUTING_GROUP = "tcs:routingGroup";
  /**
   * The default value of a vehicle's routing group.
   */
  public static final int DEFAULT_ROUTING_GROUP = 0;
  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultRouter.class);
  /**
   * Whether to explicitly look for a (static or computed) route even if the
   * destination position is the source position.
   */
  private final boolean routeToCurrentPosition;
  /**
   * The kernel providing the model data.
   */
  private final LocalKernel kernel;
  /**
   * A builder for constructing our routing tables.
   */
  private final RoutingTableBuilder tableBuilder;
  /**
   * The routes selected for each vehicle.
   */
  private final Map<Vehicle, List<DriveOrder>> routesByVehicle = new ConcurrentHashMap<>();
  /**
   * The routing nets by vehicle routing group.
   */
  private final Map<Integer, RoutingTable> netsByVehicle = new ConcurrentHashMap<>();
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel providing the model data.
   * @param tableBuilder A builder for constructing routing tables.
   * @param routeToCurrentPosition Whether to explicitly look for a (static or
   * computed) route even if the destination position is the source position.
   */
  @Inject
  public DefaultRouter(LocalKernel kernel,
                       RoutingTableBuilder tableBuilder,
                       @RouteToCurrentPos boolean routeToCurrentPosition) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.tableBuilder = requireNonNull(tableBuilder, "tableBuilder");
    this.routeToCurrentPosition = routeToCurrentPosition;
  }

  @Override
  public void initialize() {
    routesByVehicle.clear();
    updateRoutingTables();
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    routesByVehicle.clear();
    netsByVehicle.clear();
    initialized = false;
  }

  @Override
  @Deprecated
  public void updateRoutingTables() {
    netsByVehicle.clear();
    for (Vehicle curVehicle : kernel.getTCSObjects(Vehicle.class)) {
      int currentGroup = getRoutingGroupOfVehicle(curVehicle);
      if (!netsByVehicle.containsKey(currentGroup)) {
        RoutingTable routingNet = tableBuilder.computeTable(curVehicle);
        netsByVehicle.put(currentGroup, routingNet);
      }
    }
    LOG.debug("Number of nets computed: {}", netsByVehicle.size());
  }

  @Override
  public Set<Vehicle> checkRoutability(TransportOrder order) {
    requireNonNull(order, "order");

    Set<Vehicle> result = new HashSet<>();
    List<DriveOrder> driveOrderList = order.getFutureDriveOrders();
    DriveOrder[] driveOrders
        = driveOrderList.toArray(new DriveOrder[driveOrderList.size()]);
    for (Map.Entry<Integer, RoutingTable> curEntry : netsByVehicle.entrySet()) {
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

  @Override
  public Optional<List<DriveOrder>> getRoute(Vehicle vehicle,
                                             Point sourcePoint,
                                             TransportOrder transportOrder) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(sourcePoint, "sourcePoint");
    requireNonNull(transportOrder, "transportOrder");

    List<DriveOrder> driveOrderList = transportOrder.getFutureDriveOrders();
    DriveOrder[] driveOrders = driveOrderList.toArray(new DriveOrder[driveOrderList.size()]);
    RoutingTable net = netsByVehicle.get(getRoutingGroupOfVehicle(vehicle));
    OrderRouteParameterStruct params = new OrderRouteParameterStruct(driveOrders, net);
    OrderRouteResultStruct resultStruct = new OrderRouteResultStruct(driveOrderList.size());
    computeCheapestOrderRoute(sourcePoint, params, 0, resultStruct);
    return (resultStruct.bestCosts == Long.MAX_VALUE)
        ? Optional.empty()
        : Optional.of(Arrays.asList(resultStruct.bestRoute));
  }

  @Override
  public Optional<Route> getRoute(Vehicle vehicle,
                                  Point sourcePoint,
                                  Point destinationPoint) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(sourcePoint, "sourcePoint");
    requireNonNull(destinationPoint, "destinationPoint");

    RoutingTable net = netsByVehicle.get(getRoutingGroupOfVehicle(vehicle));
    long costs = net.getCosts(sourcePoint, destinationPoint);
    if (costs == INFINITE_COSTS) {
      return Optional.empty();
    }
    List<Route.Step> steps = net.getRouteSteps(sourcePoint, destinationPoint);
    if (steps.isEmpty()) {
      // If the list of steps is empty, we're already at the destination point
      // Create a single step without a path.
      steps.add(new Route.Step(null, null, sourcePoint, Vehicle.Orientation.UNDEFINED, 0));
    }
    return Optional.of(new Route(steps, costs));
  }

  @Override
  public long getCosts(Vehicle vehicle,
                       Point sourcePoint,
                       Point destinationPoint) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(sourcePoint, "sourcePoint");
    requireNonNull(destinationPoint, "destinationPoint");

    return netsByVehicle.get(getRoutingGroupOfVehicle(vehicle))
        .getCosts(sourcePoint, destinationPoint);
  }

  @Override
  public long getCostsByPointRef(Vehicle vehicle,
                                 TCSObjectReference<Point> srcPointRef,
                                 TCSObjectReference<Point> dstPointRef) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(srcPointRef, "srcPointRef");
    requireNonNull(dstPointRef, "dstPointRef");

    return netsByVehicle.get(getRoutingGroupOfVehicle(vehicle))
        .getCosts(srcPointRef, dstPointRef);
  }

  @Override
  public long getCosts(Vehicle vehicle,
                       TCSObjectReference<Location> srcRef,
                       TCSObjectReference<Location> destRef) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(srcRef, "srcRef");
    requireNonNull(destRef, "destRef");

    // Get all attached links for source and destination
    Set<Link> srcLinks = kernel.getTCSObject(Location.class, srcRef).getAttachedLinks();
    Set<Link> destLinks = kernel.getTCSObject(Location.class, destRef).getAttachedLinks();

    // Find the cheapest destination link to be used
    long costs = Long.MAX_VALUE;
    for (Link srcLink : srcLinks) {
      for (Link destLink : destLinks) {
        long linkCosts = getCosts(vehicle,
                                  kernel.getTCSObject(Point.class, srcLink.getPoint()),
                                  kernel.getTCSObject(Point.class, destLink.getPoint()));
        costs = Math.min(costs, linkCosts);
      }
    }
    return costs;
  }

  @Override
  public void selectRoute(Vehicle vehicle, List<DriveOrder> driveOrders) {
    requireNonNull(vehicle, "vehicle");

    if (driveOrders == null) {
      // XXX Should we remember the vehicle's current position, maybe?
      routesByVehicle.remove(vehicle);
    }
    else {
      routesByVehicle.put(vehicle, driveOrders);
    }
  }

  @Override
  public Map<Vehicle, List<DriveOrder>> getSelectedRoutes() {
    return Collections.unmodifiableMap(routesByVehicle);
  }

  @Override
  public Set<Point> getTargetedPoints() {
    Set<Point> result = new HashSet<>();
    for (List<DriveOrder> curOrderList : routesByVehicle.values()) {
      DriveOrder finalOrder = curOrderList.get(curOrderList.size() - 1);
      result.add(finalOrder.getRoute().getFinalDestinationPoint());
    }
    return result;
  }

  @Override
  public String getInfo() {
    return "Computed nets/routing tables: " + netsByVehicle.size();
  }

  /**
   * Checks if a route exists for a vehicle of a given type which allows the
   * vehicle to process a given list of drive orders.
   *
   * @param startPoint The point at which the route is supposed to start.
   * @param driveOrders The list of drive orders, in the order they are to be
   * processed.
   * @param nextHopIndex The index of the next drive order in the list.
   * @param net The routing net to use.
   * @return <code>true</code> if, and only if, at least one route exists which
   * would allow a vehicle of the given type to process the whole list of drive
   * orders.
   */
  private boolean isRoutable(Point startPoint,
                             DriveOrder[] driveOrders,
                             int nextHopIndex,
                             RoutingTable net) {
    assert startPoint != null;
    assert driveOrders != null;
    assert net != null;

    if (nextHopIndex < driveOrders.length) {
      for (Point curPoint : getDestinationPoints(driveOrders[nextHopIndex])) {
        // Check if there is a route from the starting point to the current
        // point and if the rest of the orders are routable from there, too.
        if (net.getCosts(startPoint, curPoint) != INFINITE_COSTS
            && isRoutable(curPoint, driveOrders, nextHopIndex + 1, net)) {
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
      if (!routeToCurrentPosition && destPoints.contains(startPoint)) {
        LOG.debug("Shortcutting route to {}", startPoint);
        destPoints.clear();
        destPoints.add(startPoint);
      }
      boolean routable = false;
      for (Point curDestPoint : destPoints) {
        final long hopCosts = params.net.getCosts(startPoint, curDestPoint);
        if (hopCosts == INFINITE_COSTS) {
          continue;
        }
        // Get the list of steps for the route of the current drive order.
        List<Route.Step> steps
            = params.net.getRouteSteps(startPoint, curDestPoint);
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
        DriveOrder hopOrder = params.driveOrders[hopIndex].clone();
        hopOrder.setRoute(hopRoute);
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
    final TCSObjectReference<Location> destLocRef = dest.getLocation();
    final String operation = dest.getOperation();
    // If the location reference is a dummy and the operation is "just move" or
    // "park the vehicle", this is an order to send the vehicle to an explicitly
    // selected point - return an appropriate set with only that point.
    if (destLocRef.isDummy()
        && (Destination.OP_MOVE.equals(operation)
            || Destination.OP_PARK.equals(operation))) {
      // Route the vehicle to an user selected point if halting is allowed there.
      Point destPoint = kernel.getTCSObject(Point.class, destLocRef.getName());
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
      final Location destLoc = kernel.getTCSObject(Location.class, destLocRef);
      final LocationType destLocType = kernel.getTCSObject(LocationType.class,
                                                           destLoc.getType());
      for (Location.Link curLink : destLoc.getAttachedLinks()) {
        // A link is acceptable if any of the following conditions are true:
        // - The destination operation is OP_NOP, which is allowed everywhere.
        // - The destination operation is explicitly allowed with the link.
        // - The link's set of allowed operations is empty and the destination
        //   operation is explicitly allowed with the location's type.
        // Furthermore, the point to be routed at must allow halting.
        if (Destination.OP_NOP.equals(operation)
            || curLink.hasAllowedOperation(operation)
            || (curLink.getAllowedOperations().isEmpty()
                && destLocType.isAllowedOperation(operation))) {
          Point destPoint = kernel.getTCSObject(Point.class, curLink.getPoint());
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
   * @param routingGroup The routint group all vehicles should be in
   * @return The vehicles which have the given routing group
   */
  private Set<Vehicle> getVehiclesByRoutingGroup(int routingGroup) {
    Set<Vehicle> result = new HashSet<>();
    for (Vehicle curVehicle : kernel.getTCSObjects(Vehicle.class)) {
      if (getRoutingGroupOfVehicle(curVehicle) == routingGroup) {
        result.add(curVehicle);
      }
    }
    return result;
  }

  /**
   * Returns the routing group of the vehicle or {@link #DEFAULT_ROUTING_GROUP} if the property
   * does not exist or is invalid.
   *
   * @param vehicle The vehicle
   * @return The routing group of the vehicle
   */
  private int getRoutingGroupOfVehicle(Vehicle vehicle) {
    int routingGroup = DEFAULT_ROUTING_GROUP;
    try {
      routingGroup = Integer.parseInt(vehicle.getProperty(PROPKEY_ROUTING_GROUP));
    }
    catch (NumberFormatException e) {
      LOG.debug("Invalid routing group '{}' for vehicle {}, using default ({}).",
                routingGroup,
                vehicle,
                DEFAULT_ROUTING_GROUP);
    }
    return routingGroup;
  }

  /**
   * Annotation type for injecting whether to route to the vehicle's current
   * position.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface RouteToCurrentPos {
    // Nothing here.
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
     * The routing net for the vehicle type.
     */
    private final RoutingTable net;

    /**
     * Creates a new OrderRouteParameterStruct.
     *
     * @param driveOrders A list of drive orders to be processed as checkpoints
     * of the route to be computed.
     * @param net The routing net for the vehicle type.
     */
    public OrderRouteParameterStruct(DriveOrder[] driveOrders,
                                     RoutingTable net) {
      this.driveOrders = requireNonNull(driveOrders, "driveOrders");
      this.net = requireNonNull(net, "net");
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
