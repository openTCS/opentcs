/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.Route.Step;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleController;
import org.opentcs.drivers.vehicle.VehicleControllerPool;
import static org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration.ReroutingImpossibleStrategy.IGNORE_PATH_LOCKS;
import static org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration.ReroutingImpossibleStrategy.PAUSE_IMMEDIATELY;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides some utility methods used for rerouting vehicles.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class RerouteUtil {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RerouteUtil.class);
  /**
   * The router.
   */
  private final Router router;
  /**
   * The vehicle controller pool.
   */
  private final VehicleControllerPool vehicleControllerPool;
  /**
   * The object service.
   */
  private final InternalTransportOrderService transportOrderService;

  private final DefaultDispatcherConfiguration configuration;

  /**
   * Creates a new instance.
   *
   * @param router The router.
   * @param vehicleControllerPool The vehicle controller pool.
   * @param transportOrderService The object service.
   * @param configuration The configuration.
   */
  @Inject
  public RerouteUtil(Router router,
                     VehicleControllerPool vehicleControllerPool,
                     InternalTransportOrderService transportOrderService,
                     DefaultDispatcherConfiguration configuration) {
    this.router = requireNonNull(router, "router");
    this.vehicleControllerPool = requireNonNull(vehicleControllerPool, "vehicleControllerPool");
    this.transportOrderService = requireNonNull(transportOrderService, "transportOrderService");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  public void reroute(Vehicle vehicle) {
    if (!vehicle.isProcessingOrder()) {
      LOG.warn("{} can't be rerouted without processing a transport order.", vehicle.getName());
      return;
    }

    TransportOrder originalOrder = transportOrderService.fetchObject(TransportOrder.class,
                                                                     vehicle.getTransportOrder());

    Point rerouteSource = getFutureOrCurrentPosition(vehicle);

    // Get all unfinished drive order of the transport order the vehicle is processing
    List<DriveOrder> unfinishedOrders = getUnfinishedDriveOrders(originalOrder);

    // Try to get a new route for the unfinished drive orders from the point
    Optional<List<DriveOrder>> optOrders = tryReroute(unfinishedOrders,
                                                      vehicle,
                                                      rerouteSource);

    // Get the drive order with the new route or stick to the old one
    List<DriveOrder> newDriveOrders;
    if (optOrders.isPresent()) {
      newDriveOrders = optOrders.get();
    }
    else {
      unfinishedOrders = updatePathLocks(unfinishedOrders);
      unfinishedOrders = markRestrictedSteps(unfinishedOrders);
      newDriveOrders = unfinishedOrders;
    }

    adjustFirstDriveOrder(newDriveOrders, vehicle, originalOrder, rerouteSource);

    LOG.debug("Adjusted route of transport order {} from point {}: {}",
              originalOrder.getName(),
              rerouteSource.getName(),
              newDriveOrders);

    updateTransportOrder(originalOrder, newDriveOrders, vehicle);
  }

  private void adjustFirstDriveOrder(List<DriveOrder> newDriveOrders,
                                     Vehicle vehicle,
                                     TransportOrder originalOrder,
                                     Point rerouteSource) {
    // If the vehicle is currently processing a (drive) order (and not waiting to get the next
    // drive order) we need to take care of it
    if (vehicle.hasProcState(Vehicle.ProcState.PROCESSING_ORDER)) {
      if (isPointDestinationOfOrder(rerouteSource, originalOrder.getCurrentDriveOrder())) {
        // The current drive order could not get rerouted, because the vehicle already
        // received all commands for it. Therefore we want to keep the original drive order.
        newDriveOrders.set(0, originalOrder.getCurrentDriveOrder());
      }
      else {
        // Restore the current drive order's history
        DriveOrder newCurrentOrder = mergeDriveOrders(originalOrder.getCurrentDriveOrder(),
                                                      newDriveOrders.get(0),
                                                      vehicle);
        newDriveOrders.set(0, newCurrentOrder);
      }
    }
  }

  private void updateTransportOrder(TransportOrder originalOrder,
                                    List<DriveOrder> newDriveOrders,
                                    Vehicle vehicle) {
    VehicleController controller = vehicleControllerPool.getVehicleController(vehicle.getName());

    // Restore the transport order's history
    List<DriveOrder> newOrders = new ArrayList<>();
    newOrders.addAll(originalOrder.getPastDriveOrders());
    newOrders.addAll(newDriveOrders);

    // Update the transport order's drive orders with the re-routed ones
    LOG.debug("{}: Updating drive orders with {}.", originalOrder.getName(), newOrders);
    transportOrderService.updateTransportOrderDriveOrders(originalOrder.getReference(),
                                                          newOrders);

    // If the vehicle is currently processing a (drive) order (and not waiting to get the next
    // drive order) we need to take care of it
    if (vehicle.hasProcState(Vehicle.ProcState.PROCESSING_ORDER)) {
      // Update the vehicle's current drive order with the new one
      controller.updateDriveOrder(newDriveOrders.get(0), originalOrder.getProperties());
    }

    // Let the router know the vehicle selected another route
    router.selectRoute(vehicle, newOrders);
  }

  /**
   * Returns a list of drive orders that yet haven't been finished for the given transport order.
   *
   * @param order The transport order to get unfinished drive orders from.
   * @return The list of unfinished drive orders.
   */
  public List<DriveOrder> getUnfinishedDriveOrders(TransportOrder order) {
    List<DriveOrder> result = new ArrayList<>();
    result.add(order.getCurrentDriveOrder());
    result.addAll(order.getFutureDriveOrders());
    return result;
  }

  /**
   * Merges the two given drive orders.
   * The merged drive order follows the route of orderA to the point where both drive orders
   * (orderA and orderB) start to diverge. From there, the merged drive order follows the route of
   * orderB.
   *
   * @param orderA A drive order.
   * @param orderB A drive order to be merged into {@code orderA}.
   * @param vehicle The vehicle to merge the drive orders for.
   * @return The (new) merged drive order.
   */
  public DriveOrder mergeDriveOrders(DriveOrder orderA, DriveOrder orderB, Vehicle vehicle) {
    // Merge the drive order routes
    Route mergedRoute = mergeRoutes(vehicle, orderA.getRoute(), orderB.getRoute());

    DriveOrder mergedOrder = new DriveOrder(orderA.getDestination())
        .withState(orderA.getState())
        .withTransportOrder(orderA.getTransportOrder())
        .withRoute(mergedRoute);

    return mergedOrder;
  }

  /**
   * Tries to re-route the given drive orders.
   *
   * @param driveOrders The drive orders to re-route.
   * @param vehicle The vehicle to re-route for.
   * @param sourcePoint The source point to re-route from.
   * @return The re-routed list of drive orders, if re-routing is possible, otherwise the original
   * list of drive orders.
   */
  public Optional<List<DriveOrder>> tryReroute(List<DriveOrder> driveOrders,
                                               Vehicle vehicle,
                                               Point sourcePoint) {
    LOG.debug("Trying to re-route drive orders for {} from {}: {}",
              vehicle.getName(),
              sourcePoint,
              driveOrders);
    Optional<List<DriveOrder>> optDriveOrders = router.getRoute(vehicle,
                                                                sourcePoint,
                                                                new TransportOrder("reroute-dummy",
                                                                                   driveOrders));
    return optDriveOrders;
  }

  /**
   * Returns the steps the given vehicle will process in the future after processing the commands
   * that have been already sent to it.
   *
   * @param vehicle The vehicle to get the future steps for.
   * @return The steps the given vehicle will process in the future or an empty list, if the given
   * vehicle isn't processing any order.
   */
  public List<Step> getFutureSteps(Vehicle vehicle) {
    TCSObjectReference<TransportOrder> orderRef = vehicle.getTransportOrder();
    if (orderRef == null) {
      LOG.debug("Vehicle {} isn't processing any order. Can't determine future steps.",
                vehicle.getName());
      return new ArrayList<>();
    }

    TransportOrder order = transportOrderService.fetchObject(TransportOrder.class, orderRef);
    VehicleController controller = vehicleControllerPool.getVehicleController(vehicle.getName());

    // Get the steps for the drive order the vehicle is currently processing
    // The transport order's drive orders and their routes can't be null at this point
    List<Step> currentSteps = order.getCurrentDriveOrder().getRoute().getSteps();

    // If movement commands have been sent to the comm adapter, trim the current steps by these.
    // Movement commands may have not been sent to the comm adapter yet, i.e. if needed resources
    // are already allocated by another vehicle.
    if (!controller.getCommandsSent().isEmpty()) {
      List<MovementCommand> commandsSent = new ArrayList<>(controller.getCommandsSent());
      MovementCommand lastCommandSent = commandsSent.get(commandsSent.size() - 1);

      // Trim the current steps / Get the steps that haven't been sent to the comm adapter yet
      currentSteps = currentSteps.subList(
          currentSteps.indexOf(lastCommandSent.getStep()) + 1, currentSteps.size());
    }

    List<Step> futureSteps = new ArrayList<>();
    futureSteps.addAll(currentSteps);

    // Add the steps from all future drive orders
    order.getFutureDriveOrders().stream()
        .map(driveOrder -> driveOrder.getRoute())
        .map(route -> route.getSteps())
        .forEach(steps -> futureSteps.addAll(steps));

    return futureSteps;
  }

  /**
   * Returns the point the given vehicle will be at after processing all commands that have been
   * currently sent to its comm adapter or its current position, if its sent queue is empty.
   *
   * @param vehicle The vehicle to get the point for.
   * @return The point.
   */
  public Point getFutureOrCurrentPosition(Vehicle vehicle) {
    VehicleController controller = vehicleControllerPool.getVehicleController(vehicle.getName());
    if (controller.getCommandsSent().isEmpty()) {
      return transportOrderService.fetchObject(Point.class, vehicle.getCurrentPosition());
    }

    List<MovementCommand> commandsSent = new ArrayList<>(controller.getCommandsSent());
    LOG.debug("Commands sent: {}", commandsSent);
    MovementCommand lastCommandSend = commandsSent.get(commandsSent.size() - 1);
    return lastCommandSend.getStep().getDestinationPoint();
  }

  /**
   * Checks if the routes of the two given lists of drive orders are equal.
   *
   * @param ordersA A list of drive orders.
   * @param ordersB A list of drive order to be compared with {@code orderA} for equality.
   * @return {@code true} if the rutes are equal to each other and {@code false} otherwise.
   */
  public boolean routesEquals(List<DriveOrder> ordersA, List<DriveOrder> ordersB) {
    List<Route> routesA = ordersA.stream()
        .map(order -> order.getRoute())
        .collect(Collectors.toList());
    List<Route> routesB = ordersB.stream()
        .map(order -> order.getRoute())
        .collect(Collectors.toList());
    return Objects.equals(routesA, routesB);
  }

  private Route mergeRoutes(Vehicle vehicle, Route routeA, Route routeB) {
    // Merge the route steps
    List<Step> mergedSteps = mergeSteps(routeA.getSteps(), routeB.getSteps());

    // Calculate the costs for merged route
    Point sourcePoint = mergedSteps.get(0).getSourcePoint();
    Point destinationPoint = mergedSteps.get(mergedSteps.size() - 1).getDestinationPoint();
    long costs = router.getCosts(vehicle, sourcePoint, destinationPoint);

    return new Route(mergedSteps, costs);
  }

  private List<Step> mergeSteps(List<Step> stepsA, List<Step> stepsB) {
    LOG.debug("Merging steps {} with {}", stepsToPaths(stepsA), stepsToPaths(stepsB));

    // Get the step where routeB starts to depart, i.e. the step where routeA and routeB share the
    // same source point
    Step branchingStep = findStepWithSource(stepsB.get(0).getSourcePoint(), stepsA);

    int branchingIndex = stepsA.indexOf(branchingStep);
    List<Step> mergedSteps = new ArrayList<>();
    mergedSteps.addAll(stepsA.subList(0, branchingIndex));
    mergedSteps.addAll(stepsB);

    // Update the steps route indices since they originate from two different drive orders
    mergedSteps = updateRouteIndices(mergedSteps);

    return mergedSteps;
  }

  private Step findStepWithSource(Point sourcePoint, List<Step> steps) {
    LOG.debug("Looking for a step with source point {} in {}",
              sourcePoint,
              stepsToPaths(steps));
    return steps.stream()
        .filter(step -> Objects.equals(step.getSourcePoint(), sourcePoint))
        .findFirst()
        .get();
  }

  private List<Step> updateRouteIndices(List<Step> steps) {
    List<Step> updatedSteps = new ArrayList<>();
    for (int i = 0; i < steps.size(); i++) {
      Step currStep = steps.get(i);
      updatedSteps.add(new Step(currStep.getPath(),
                                currStep.getSourcePoint(),
                                currStep.getDestinationPoint(),
                                currStep.getVehicleOrientation(),
                                i,
                                currStep.isExecutionAllowed()));
    }
    return updatedSteps;
  }

  private List<Path> stepsToPaths(List<Step> steps) {
    return steps.stream()
        .map(step -> step.getPath())
        .collect(Collectors.toList());
  }

  private boolean isPointDestinationOfOrder(Point point, DriveOrder order) {
    if (point == null || order == null) {
      return false;
    }
    if (order.getRoute() == null) {
      return false;
    }
    return Objects.equals(point, order.getRoute().getFinalDestinationPoint());
  }

  private List<DriveOrder> updatePathLocks(List<DriveOrder> orders) {
    List<DriveOrder> updatedOrders = new ArrayList<>();

    for (DriveOrder order : orders) {
      List<Step> updatedSteps = new ArrayList<>();

      for (Step step : order.getRoute().getSteps()) {
        Path path = transportOrderService.fetchObject(Path.class, step.getPath().getReference());
        updatedSteps.add(new Route.Step(path,
                                        step.getSourcePoint(),
                                        step.getDestinationPoint(),
                                        step.getVehicleOrientation(),
                                        step.getRouteIndex()));
      }

      Route updatedRoute = new Route(updatedSteps, order.getRoute().getCosts());

      DriveOrder updatedOrder = new DriveOrder(order.getDestination())
          .withRoute(updatedRoute)
          .withState(order.getState())
          .withTransportOrder(order.getTransportOrder());
      updatedOrders.add(updatedOrder);
    }

    return updatedOrders;
  }

  private List<DriveOrder> markRestrictedSteps(List<DriveOrder> orders) {
    if (configuration.reroutingImpossibleStrategy() == IGNORE_PATH_LOCKS) {
      return orders;
    }
    if (!containsLockedPath(orders)) {
      return orders;
    }

    List<DriveOrder> updatedOrders = new ArrayList<>();
    boolean allowed = configuration.reroutingImpossibleStrategy() != PAUSE_IMMEDIATELY;

    for (DriveOrder order : orders) {
      List<Step> updatedSteps = new ArrayList<>();

      for (Step step : order.getRoute().getSteps()) {
        allowed = allowed && !step.getPath().isLocked();

        LOG.debug("Marking path '{}' allowed: {}", step.getPath(), allowed);
        updatedSteps.add(new Step(step.getPath(),
                                  step.getSourcePoint(),
                                  step.getDestinationPoint(),
                                  step.getVehicleOrientation(),
                                  step.getRouteIndex(),
                                  allowed));
      }

      Route updatedRoute = new Route(updatedSteps, order.getRoute().getCosts());

      DriveOrder updatedOrder = new DriveOrder(order.getDestination())
          .withRoute(updatedRoute)
          .withState(order.getState())
          .withTransportOrder(order.getTransportOrder());
      updatedOrders.add(updatedOrder);
    }

    return updatedOrders;
  }

  private boolean containsLockedPath(List<DriveOrder> orders) {
    return orders.stream()
        .map(order -> order.getRoute().getSteps())
        .flatMap(steps -> steps.stream())
        .filter(step -> step.getPath().isLocked())
        .findAny()
        .isPresent();
  }
}
