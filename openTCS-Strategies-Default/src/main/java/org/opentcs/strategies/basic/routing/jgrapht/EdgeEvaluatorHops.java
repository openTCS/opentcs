/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import org.opentcs.data.model.Vehicle;

/**
 * Uses a weight of 1 for every edge.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class EdgeEvaluatorHops
    implements EdgeEvaluator {

  public EdgeEvaluatorHops() {
  }

  @Override
  public double computeWeight(ModelEdge edge, Vehicle vehicle) {
    return 1;
  }
}
