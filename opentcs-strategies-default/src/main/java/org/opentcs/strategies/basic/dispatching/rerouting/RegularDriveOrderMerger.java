// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.rerouting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DriveOrderMerger} implementation for {@link ReroutingType#REGULAR}.
 * <p>
 * Merges two drive orders so that the merged drive order follows the route of {@code orderA} up to
 * the point where both drive orders ({@code orderA} and {@code orderB}) start to diverge. From
 * there, the merged drive order follows the route of {@code orderB}.
 */
public class RegularDriveOrderMerger
    extends
      AbstractDriveOrderMerger {

  private static final Logger LOG = LoggerFactory.getLogger(RegularDriveOrderMerger.class);

  /**
   * Creates a new instance.
   */
  public RegularDriveOrderMerger() {
  }

  @Override
  protected List<Route.Step> mergeSteps(
      List<Route.Step> stepsA,
      List<Route.Step> stepsB,
      int currentRouteStepIndex
  ) {
    LOG.debug("Merging steps {} with {}", stepsToPaths(stepsA), stepsToPaths(stepsB));
    List<Route.Step> mergedSteps = new ArrayList<>();

    // Get the step where stepsB starts to diverge from stepsA (i.e. the step where routeA and
    // routeB share the same source point).
    Route.Step divergingStep = findStepWithSource(stepsB.get(0).getSourcePoint(), stepsA);
    int divergingIndex = stepsA.indexOf(divergingStep);
    mergedSteps.addAll(stepsA.subList(0, divergingIndex));

    // Set the rerouting type for the first step in the new route.
    List<Route.Step> modifiedStepsB = new ArrayList<>(stepsB);
    modifiedStepsB.set(0, stepsB.getFirst().withReroutingType(ReroutingType.REGULAR));

    mergedSteps.addAll(modifiedStepsB);

    // Update the steps route indices since they originate from two different drive orders.
    mergedSteps = updateRouteIndices(mergedSteps);

    return mergedSteps;
  }

  private Route.Step findStepWithSource(Point sourcePoint, List<Route.Step> steps) {
    LOG.debug(
        "Looking for a step with source point {} in {}",
        sourcePoint,
        stepsToPaths(steps)
    );
    // Start searching from the back of the route/steps to account for loops in the route. Although
    // we can't have loops in a route returned directly by the router, loops are possible when
    // rerouting is involved (e.g. when the vehicle has to perform some backtracking in order to
    // "get on the new route"). This means that the given source point can appear multiple times in
    // the route, but only in the "old" part of the route before the merge; thus, we only look for
    // its latest occurrence here.
    return steps.reversed().stream()
        .filter(step -> Objects.equals(step.getSourcePoint(), sourcePoint))
        .findFirst()
        .orElseThrow();
  }
}
