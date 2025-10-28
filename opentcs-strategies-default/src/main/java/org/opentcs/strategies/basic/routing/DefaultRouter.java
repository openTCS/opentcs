// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing;

import static java.util.Objects.requireNonNull;
import static org.opentcs.strategies.basic.routing.PointRouter.INFINITE_COSTS;
import static org.opentcs.util.Assertions.checkArgument;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.routing.GroupMapper;
import org.opentcs.components.kernel.services.InternalTCSObjectService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.routing.jgrapht.PointRouterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic {@link Router} implementation.
 */
public class DefaultRouter
    implements
      Router {

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
  private final InternalTCSObjectService objectService;
  /**
   * Provides point routers for vehicles.
   */
  private final PointRouterProvider pointRouterProvider;
  /**
   * Used to map vehicles to their routing groups.
   */
  private final GroupMapper routingGroupMapper;
  /**
   * The routes selected for each vehicle.
   */
  private final Map<Vehicle, List<DriveOrder>> routesByVehicle = new ConcurrentHashMap<>();
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service providing the model data.
   * @param pointRouterProvider Provides point routers for vehicles.
   * @param routingGroupMapper Used to map vehicles to their routing groups.
   * @param configuration This class's configuration.
   */
  @Inject
  public DefaultRouter(
      InternalTCSObjectService objectService,
      PointRouterProvider pointRouterProvider,
      GroupMapper routingGroupMapper,
      DefaultRouterConfiguration configuration
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.pointRouterProvider = requireNonNull(pointRouterProvider, "pointRouterProvider");
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
      pointRouterProvider.invalidate();
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
      pointRouterProvider.invalidate();
      initialized = false;
    }
  }

  @Override
  public void updateRoutingTopology(Set<Path> paths) {
    requireNonNull(paths, "paths");

    synchronized (this) {
      pointRouterProvider.updateRoutingTopology(paths);
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

      for (Map.Entry<String, PointRouter> curEntry : pointRouterProvider
          .getPointRoutersByVehicleGroup().entrySet()) {
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
  public boolean checkGeneralRoutability(TransportOrder order) {
    requireNonNull(order, "order");

    synchronized (this) {
      List<DriveOrder> driveOrderList = order.getFutureDriveOrders();
      DriveOrder[] driveOrders
          = driveOrderList.toArray(new DriveOrder[driveOrderList.size()]);

      PointRouter generalPointRouter = pointRouterProvider.getGeneralPointRouter(order);

      for (Point curStartPoint : getDestinationPoints(driveOrders[0])) {
        if (!isRoutable(curStartPoint, driveOrders, 1, generalPointRouter)) {
          return false;
        }
      }
      return true;
    }
  }

  @Override
  public Set<List<Route>> getRoutes(
      Vehicle vehicle,
      Point sourcePoint,
      TransportOrder transportOrder,
      int maxRouteCount
  ) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(sourcePoint, "sourcePoint");
    requireNonNull(transportOrder, "transportOrder");
    checkArgument(maxRouteCount > 0, "maxRouteCount must be greater than zero");

    synchronized (this) {
      // TODO: Once maxRouteCount is actually used, ensure to cap it at
      //       DefaultRouterConfiguration.routeComputationLimit() using Math.min().
      List<DriveOrder> driveOrderList = transportOrder.getFutureDriveOrders();
      DriveOrder[] driveOrders = driveOrderList.toArray(new DriveOrder[driveOrderList.size()]);
      PointRouter pointRouter = pointRouterProvider.getPointRouterForVehicle(
          vehicle,
          transportOrder
      );
      OrderRouteParameterStruct params = new OrderRouteParameterStruct(driveOrders, pointRouter);
      OrderRouteResultStruct resultStruct = new OrderRouteResultStruct(driveOrderList.size());
      computeCheapestOrderRoute(sourcePoint, params, 0, resultStruct);
      return (resultStruct.bestCosts == Long.MAX_VALUE)
          ? Set.of()
          : Set.of(List.of(resultStruct.bestRoute));
    }
  }

  @Override
  public Set<Route> getRoutes(
      Vehicle vehicle,
      Point sourcePoint,
      Point destinationPoint,
      Set<TCSResourceReference<?>> resourcesToAvoid,
      int maxRouteCount
  ) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(sourcePoint, "sourcePoint");
    requireNonNull(destinationPoint, "destinationPoint");
    requireNonNull(resourcesToAvoid, "resourcesToAvoid");
    checkArgument(maxRouteCount > 0, "maxRouteCount must be greater than zero");

    synchronized (this) {
      // TODO: Once maxRouteCount is actually used, ensure to cap it at
      //       DefaultRouterConfiguration.routeComputationLimit() using Math.min().
      return Optional.ofNullable(
          pointRouterProvider.getPointRouterForVehicle(vehicle, resourcesToAvoid)
              .getRouteSteps(sourcePoint, destinationPoint)
      )
          .map(steps -> {
            if (steps.isEmpty()) {
              return List.of(
                  // If the list of steps is empty, we're already at the destination point create a
                  // single step without a path.
                  new Route.Step(null, null, sourcePoint, Vehicle.Orientation.UNDEFINED, 0, 0)
              );
            }
            else {
              return steps;
            }
          })
          .map(Route::new)
          .map(Set::of)
          .orElse(Set.of());
    }
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
  private boolean isRoutable(
      Point startPoint,
      DriveOrder[] driveOrders,
      int nextHopIndex,
      PointRouter pointRouter
  ) {
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
  private void computeCheapestOrderRoute(
      Point startPoint,
      OrderRouteParameterStruct params,
      int hopIndex,
      OrderRouteResultStruct result
  ) {
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
          steps.add(new Route.Step(null, null, startPoint, Vehicle.Orientation.UNDEFINED, 0, 0));
        }
        // Create a route from the list of steps gathered.
        Route hopRoute = new Route(steps);
        // Place the computed route in the result struct.
        result.currentRoute[hopIndex] = hopRoute;
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
      Point destPoint
          = objectService.fetch(Point.class, dest.getDestination().getName()).orElseThrow();
      final Set<Point> result = new HashSet<>();
      result.add(destPoint);
      return result;
    }
    // If it's a "normal" transport order, look for destination points adjacent
    // to the destination location.
    else if (dest.getDestination().getReferentClass() == Location.class) {
      final Set<Point> result = new HashSet<>();
      final Location destLoc = objectService.fetch(
          Location.class,
          dest.getDestination().getName()
      ).orElseThrow();
      final LocationType destLocType = objectService.fetch(
          LocationType.class,
          destLoc.getType()
      ).orElseThrow();
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
          Point destPoint = objectService.fetch(Point.class, curLink.getPoint()).orElseThrow();
          result.add(destPoint);
        }
      }
      return result;
    }
    else {
      return new HashSet<>();
    }
  }

  /**
   * Returns all vehicles within the given routing group.
   *
   * @param routingGroup The routing group the returned vehicles should belong to.
   * @return The vehicles which have the given routing group
   */
  private Set<Vehicle> getVehiclesByRoutingGroup(String routingGroup) {
    return objectService.fetch(Vehicle.class)
        .stream()
        .filter(vehicle -> Objects.equals(routingGroupMapper.apply(vehicle), routingGroup))
        .collect(Collectors.toSet());
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
    OrderRouteParameterStruct(
        DriveOrder[] driveOrders,
        PointRouter pointRouter
    ) {
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
    private Route[] currentRoute;
    /**
     * The costs of the route currently being examined.
     */
    private long currentCosts;
    /**
     * The best route found so far.
     */
    private Route[] bestRoute;
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
    OrderRouteResultStruct(int driveOrderCount) {
      currentRoute = new Route[driveOrderCount];
      currentCosts = 0;
      bestRoute = new Route[driveOrderCount];
      bestCosts = Long.MAX_VALUE;
    }
  }
}
