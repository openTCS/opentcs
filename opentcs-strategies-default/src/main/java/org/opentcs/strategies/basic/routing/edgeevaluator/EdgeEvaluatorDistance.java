// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.edgeevaluator;

import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.components.kernel.routing.EdgeEvaluator;
import org.opentcs.data.model.Vehicle;

/**
 * Uses an edge's length as its weight.
 */
public class EdgeEvaluatorDistance
    implements
      EdgeEvaluator {

  /**
   * A key used for selecting this evaluator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "DISTANCE";

  public EdgeEvaluatorDistance() {
  }

  @Override
  public void onGraphComputationStart(Vehicle vehicle) {
  }

  @Override
  public void onGraphComputationEnd(Vehicle vehicle) {
  }

  @Override
  public double computeWeight(Edge edge, Vehicle vehicle) {
    return edge.getPath().getLength();
  }
}
