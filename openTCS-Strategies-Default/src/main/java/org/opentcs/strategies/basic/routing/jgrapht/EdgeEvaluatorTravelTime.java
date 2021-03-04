/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import org.opentcs.data.model.Vehicle;
import static org.opentcs.strategies.basic.routing.PointRouter.INFINITE_COSTS;

/**
 * Uses the estimated travel time (length/maximum velocity) for an edge as its weight.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class EdgeEvaluatorTravelTime
    implements EdgeEvaluator {

  public EdgeEvaluatorTravelTime() {
  }

  @Override
  public double computeWeight(ModelEdge edge, Vehicle vehicle) {
    int maxVelocity;
    if (edge.isTravellingReverse()) {
      maxVelocity = Math.min(vehicle.getMaxReverseVelocity(),
                             edge.getModelPath().getMaxReverseVelocity());
    }
    else {
      maxVelocity = Math.min(vehicle.getMaxVelocity(), edge.getModelPath().getMaxVelocity());
    }
    return (maxVelocity == 0) ? INFINITE_COSTS : edge.getModelPath().getLength() / maxVelocity;
  }
}
