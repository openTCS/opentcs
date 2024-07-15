/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.rerouting;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.routing.ResourceAvoidanceExtractor;

/**
 * An abstract implementation of {@link DriveOrderMerger} defining the basic merging algorithm.
 */
public abstract class AbstractDriveOrderMerger
    implements
      DriveOrderMerger {

  private final Router router;
  private final ResourceAvoidanceExtractor resourceAvoidanceExtractor;

  /**
   * Creates a new instance.
   *
   * @param router The router to use.
   * @param resourceAvoidanceExtractor Extracts resources to be avoided from transport orders.
   */
  protected AbstractDriveOrderMerger(
      Router router,
      ResourceAvoidanceExtractor resourceAvoidanceExtractor
  ) {
    this.router = requireNonNull(router, "router");
    this.resourceAvoidanceExtractor
        = requireNonNull(resourceAvoidanceExtractor, "resourceAvoidanceExtractor");
  }

  @Override
  public DriveOrder mergeDriveOrders(
      @Nonnull
      DriveOrder orderA,
      @Nonnull
      DriveOrder orderB,
      @Nonnull
      TransportOrder originalOrder,
      int currentRouteStepIndex,
      Vehicle vehicle
  ) {
    requireNonNull(orderA, "orderA");
    requireNonNull(orderB, "orderB");
    requireNonNull(originalOrder, "originalOrder");

    return new DriveOrder(orderA.getDestination())
        .withState(orderA.getState())
        .withTransportOrder(orderA.getTransportOrder())
        .withRoute(
            mergeRoutes(
                orderA.getRoute(),
                orderB.getRoute(),
                originalOrder,
                currentRouteStepIndex,
                vehicle
            )
        );
  }

  /**
   * Merges the two given {@link Route}s.
   *
   * @param routeA A route.
   * @param routeB A route to be merged with {@code routeA}.
   * @param originalOrder The transport order to merge the drive orders for.
   * @param currentRouteStepIndex The index of the last route step travelled for {@code routeA}.
   * @param vehicle The {@link Vehicle} to merge the routes for.
   * @return The (new) merged route.
   */
  protected Route mergeRoutes(
      Route routeA,
      Route routeB,
      TransportOrder originalOrder,
      int currentRouteStepIndex,
      Vehicle vehicle
  ) {
    // Merge the route steps
    List<Route.Step> mergedSteps = mergeSteps(
        routeA.getSteps(),
        routeB.getSteps(),
        currentRouteStepIndex
    );

    // Calculate the costs for merged route
    return new Route(
        mergedSteps,
        router.getCosts(
            vehicle,
            mergedSteps.get(0).getSourcePoint(),
            mergedSteps.get(mergedSteps.size() - 1).getDestinationPoint(),
            resourceAvoidanceExtractor
                .extractResourcesToAvoid(originalOrder)
                .toResourceReferenceSet()
        )
    );
  }

  /**
   * Merges the two given lists of {@link Route.Step}s.
   *
   * @param stepsA A list of steps.
   * @param stepsB A list of steps to be merged with {@code stepsA}.
   * @param currentRouteStepIndex The index of the last route step travelled for {@code stepsA}.
   * @return The (new) merged list of steps.
   */
  protected abstract List<Route.Step> mergeSteps(
      List<Route.Step> stepsA,
      List<Route.Step> stepsB,
      int currentRouteStepIndex
  );

  protected List<Route.Step> updateRouteIndices(List<Route.Step> steps) {
    List<Route.Step> updatedSteps = new ArrayList<>();
    for (int i = 0; i < steps.size(); i++) {
      Route.Step currStep = steps.get(i);
      updatedSteps.add(
          new Route.Step(
              currStep.getPath(),
              currStep.getSourcePoint(),
              currStep.getDestinationPoint(),
              currStep.getVehicleOrientation(),
              i,
              currStep.isExecutionAllowed(),
              currStep.getReroutingType()
          )
      );
    }
    return updatedSteps;
  }

  protected List<Path> stepsToPaths(List<Route.Step> steps) {
    return steps.stream()
        .map(step -> step.getPath())
        .collect(Collectors.toList());
  }
}
