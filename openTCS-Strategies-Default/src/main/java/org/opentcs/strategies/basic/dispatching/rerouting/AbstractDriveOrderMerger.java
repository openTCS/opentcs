/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.rerouting;

import java.util.List;
import static java.util.Objects.requireNonNull;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;

/**
 * An abstract implementation of {@link DriveOrderMerger} defining the basic merging algorithm.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public abstract class AbstractDriveOrderMerger
    implements DriveOrderMerger {

  private final Router router;

  /**
   * Creates a new instance.
   *
   * @param router The router to use.
   */
  protected AbstractDriveOrderMerger(Router router) {
    this.router = requireNonNull(router, "router");
  }

  @Override
  public DriveOrder mergeDriveOrders(DriveOrder orderA, DriveOrder orderB, Vehicle vehicle) {
    // Merge the drive order routes
    Route mergedRoute = mergeRoutes(orderA.getRoute(), orderB.getRoute(), vehicle);

    DriveOrder mergedOrder = new DriveOrder(orderA.getDestination())
        .withState(orderA.getState())
        .withTransportOrder(orderA.getTransportOrder())
        .withRoute(mergedRoute);

    return mergedOrder;
  }

  /**
   * Merges the two given {@link Route}s.
   *
   * @param routeA A route.
   * @param routeB A route to be merged with {@code routeA}.
   * @param vehicle The {@link Vehicle} to merge the routes for.
   * @return The (new) merged route.
   */
  protected Route mergeRoutes(Route routeA, Route routeB, Vehicle vehicle) {
    // Merge the route steps
    List<Route.Step> mergedSteps = mergeSteps(routeA.getSteps(), routeB.getSteps());

    // Calculate the costs for merged route
    return new Route(
        mergedSteps,
        router.getCosts(vehicle,
                        mergedSteps.get(0).getSourcePoint(),
                        mergedSteps.get(mergedSteps.size() - 1).getDestinationPoint())
    );
  }

  /**
   * Merges the two given lists of {@link Route.Step}s.
   *
   * @param stepsA A list of steps.
   * @param stepsB A list of steps to be merged with {@code stepsA}.
   * @return The (new) merged list of steps.
   */
  protected abstract List<Route.Step> mergeSteps(List<Route.Step> stepsA, List<Route.Step> stepsB);
}
