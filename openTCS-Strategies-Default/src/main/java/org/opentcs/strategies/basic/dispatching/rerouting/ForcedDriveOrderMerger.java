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
import javax.inject.Inject;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DriveOrderMerger} implementation for {@link ReroutingType#FORCED}.
 * <p>
 * Merges two drive orders so that the merged drive order follows the route of {@code orderA} up to
 * the current route progress index reported by the vehicle that is processing the drive order. From
 * there, the merged drive order follows the route of {@code orderB}. This means that the merged
 * drive order may contain a gap/may not be continuous.
 */
public class ForcedDriveOrderMerger
    extends AbstractDriveOrderMerger {

  private static final Logger LOG = LoggerFactory.getLogger(ForcedDriveOrderMerger.class);

  /**
   * Creates a new instance.
   *
   * @param router The router to use.
   */
  @Inject
  public ForcedDriveOrderMerger(Router router) {
    super(router);
  }

  @Override
  protected List<Route.Step> mergeSteps(List<Route.Step> stepsA,
                                        List<Route.Step> stepsB,
                                        int currentRouteStepIndex) {
    LOG.debug("Merging steps {} with {}", stepsToPaths(stepsA), stepsToPaths(stepsB));
    List<Route.Step> mergedSteps = new ArrayList<>();

    // Get the steps that the vehicle has already travelled.
    mergedSteps.addAll(stepsA.subList(0, currentRouteStepIndex + 1));

    // Set the rerouting type for the first step in the new route.
    Route.Step firstStepOfNewRoute = stepsB.get(0);
    List<Route.Step> modifiedStepsB = new ArrayList<>(stepsB);
    modifiedStepsB.set(0, new Route.Step(firstStepOfNewRoute.getPath(),
                                         firstStepOfNewRoute.getSourcePoint(),
                                         firstStepOfNewRoute.getDestinationPoint(),
                                         firstStepOfNewRoute.getVehicleOrientation(),
                                         firstStepOfNewRoute.getRouteIndex(),
                                         firstStepOfNewRoute.isExecutionAllowed(),
                                         ReroutingType.FORCED));

    mergedSteps.addAll(modifiedStepsB);

    // Update the steps route indices since they originate from two different drive orders
    mergedSteps = updateRouteIndices(mergedSteps);

    return mergedSteps;
  }
}
