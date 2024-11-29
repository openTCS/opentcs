// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.rerouting;

import java.util.ArrayList;
import java.util.List;
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
    extends
      AbstractDriveOrderMerger {

  private static final Logger LOG = LoggerFactory.getLogger(ForcedDriveOrderMerger.class);

  /**
   * Creates a new instance.
   */
  public ForcedDriveOrderMerger() {
  }

  @Override
  protected List<Route.Step> mergeSteps(
      List<Route.Step> stepsA,
      List<Route.Step> stepsB,
      int currentRouteStepIndex
  ) {
    LOG.debug("Merging steps {} with {}", stepsToPaths(stepsA), stepsToPaths(stepsB));
    List<Route.Step> mergedSteps = new ArrayList<>();

    // Get the steps that the vehicle has already travelled.
    mergedSteps.addAll(stepsA.subList(0, currentRouteStepIndex + 1));

    // Set the rerouting type for the first step in the new route.
    List<Route.Step> modifiedStepsB = new ArrayList<>(stepsB);
    modifiedStepsB.set(0, stepsB.getFirst().withReroutingType(ReroutingType.FORCED));

    mergedSteps.addAll(modifiedStepsB);

    // Update the steps route indices since they originate from two different drive orders
    mergedSteps = updateRouteIndices(mergedSteps);

    return mergedSteps;
  }
}
