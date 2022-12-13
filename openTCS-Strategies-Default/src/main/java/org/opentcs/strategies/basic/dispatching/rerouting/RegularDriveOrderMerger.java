/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.rerouting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The regular/default {@link DriveOrderMerger}.
 * <p>
 * Merges two drive orders so that the merged drive order follows the route of {@code orderA} to the
 * point where both drive orders ({@code orderA} and {@code orderB}) start to diverge. From there,
 * the merged drive order follows the route of {@code orderB}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class RegularDriveOrderMerger
    extends AbstractDriveOrderMerger {

  private static final Logger LOG = LoggerFactory.getLogger(RegularDriveOrderMerger.class);

  /**
   * Creates a new instance.
   *
   * @param router The router to use.
   */
  @Inject
  public RegularDriveOrderMerger(Router router) {
    super(router);
  }

  @Override
  protected List<Route.Step> mergeSteps(List<Route.Step> stepsA, List<Route.Step> stepsB) {
    LOG.debug("Merging steps {} with {}", stepsToPaths(stepsA), stepsToPaths(stepsB));

    // Get the step where stepsB starts to depart from stepsA (i.e. the step where routeA and routeB
    // share the same source point).
    Route.Step branchingStep = findStepWithSource(stepsB.get(0).getSourcePoint(), stepsA);

    int branchingIndex = stepsA.indexOf(branchingStep);
    List<Route.Step> mergedSteps = new ArrayList<>();
    mergedSteps.addAll(stepsA.subList(0, branchingIndex));
    mergedSteps.addAll(stepsB);

    // Update the steps route indices since they originate from two different drive orders.
    mergedSteps = updateRouteIndices(mergedSteps);

    return mergedSteps;
  }

  private Route.Step findStepWithSource(Point sourcePoint, List<Route.Step> steps) {
    LOG.debug("Looking for a step with source point {} in {}",
              sourcePoint,
              stepsToPaths(steps));
    return steps.stream()
        .filter(step -> Objects.equals(step.getSourcePoint(), sourcePoint))
        .findFirst()
        .get();
  }

  private List<Route.Step> updateRouteIndices(List<Route.Step> steps) {
    List<Route.Step> updatedSteps = new ArrayList<>();
    for (int i = 0; i < steps.size(); i++) {
      Route.Step currStep = steps.get(i);
      updatedSteps.add(new Route.Step(currStep.getPath(),
                                      currStep.getSourcePoint(),
                                      currStep.getDestinationPoint(),
                                      currStep.getVehicleOrientation(),
                                      i,
                                      currStep.isExecutionAllowed()));
    }
    return updatedSteps;
  }

  private List<Path> stepsToPaths(List<Route.Step> steps) {
    return steps.stream()
        .map(step -> step.getPath())
        .collect(Collectors.toList());
  }
}
