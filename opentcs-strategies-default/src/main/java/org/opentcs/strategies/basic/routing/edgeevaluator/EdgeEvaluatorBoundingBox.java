// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.edgeevaluator;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.components.kernel.routing.EdgeEvaluator;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.routing.edgeevaluator.BoundingBoxProtrusionCheck.BoundingBoxProtrusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compares the bounding box of a vehicle with the maximum allowed bounding box at the destination
 * point of an edge and uses {@link Double#POSITIVE_INFINITY} as the edge's weight (effectively
 * excluding the edge from routing) if the vehicle's bounding box protrudes the one of the point;
 * otherwise, it uses 0.
 */
public class EdgeEvaluatorBoundingBox
    implements
      EdgeEvaluator {

  /**
   * A key used for selecting this evaluator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "BOUNDING_BOX";
  private static final Logger LOG = LoggerFactory.getLogger(EdgeEvaluatorBoundingBox.class);
  private final TCSObjectService objectService;
  private final BoundingBoxProtrusionCheck protrusionCheck;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service.
   * @param protrusionCheck Checks whether one bounding box protrudes beyond another one.
   */
  @Inject
  public EdgeEvaluatorBoundingBox(
      TCSObjectService objectService,
      BoundingBoxProtrusionCheck protrusionCheck
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.protrusionCheck = requireNonNull(protrusionCheck, "protrusionCheck");
  }

  @Override
  public void onGraphComputationStart(
      @Nonnull
      Vehicle vehicle
  ) {
  }

  @Override
  public void onGraphComputationEnd(
      @Nonnull
      Vehicle vehicle
  ) {
  }

  @Override
  public double computeWeight(
      @Nonnull
      Edge edge,
      @Nonnull
      Vehicle vehicle
  ) {
    Point targetPoint = objectService.fetch(Point.class, edge.getTargetVertex()).orElseThrow();
    BoundingBoxProtrusion protrusion = protrusionCheck.checkProtrusion(
        vehicle.getBoundingBox(), targetPoint.getMaxVehicleBoundingBox()
    );

    if (protrusion.protrudesAnywhere()) {
      LOG.debug(
          "Excluding edge '{}'. Bounding box of '{}' > max bounding box at '{}': {} > {}",
          edge,
          vehicle.getName(),
          targetPoint.getName(),
          vehicle.getBoundingBox(),
          targetPoint.getMaxVehicleBoundingBox()
      );
      return Double.POSITIVE_INFINITY;
    }

    return 0;
  }
}
