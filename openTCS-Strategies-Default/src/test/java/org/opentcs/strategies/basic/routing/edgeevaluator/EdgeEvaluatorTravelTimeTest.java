/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.edgeevaluator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import static org.opentcs.strategies.basic.routing.PointRouter.INFINITE_COSTS;

/**
 */
class EdgeEvaluatorTravelTimeTest {

  private EdgeEvaluatorTravelTime edgeEvaluator;

  @BeforeEach
  void setUp() {
    edgeEvaluator = new EdgeEvaluatorTravelTime();
  }

  @Test
  void computeTravelTime() {
    Edge edge = new Edge(
        new Path("pathName",
                 new Point("srcPoint").getReference(),
                 new Point("dstPoint").getReference())
            .withLength(10000)
            .withMaxVelocity(1000)
            .withMaxReverseVelocity(500),
        false);
    Vehicle vehicle = new Vehicle("someVehicle");

    // Length is 10 meters, maximum velocity is 1 m/s. -> The weight should be 10 (seconds).
    assertEquals(10.0, edgeEvaluator.computeWeight(edge, vehicle), 0.0);

    Edge reverseEdge = new Edge(edge.getPath(), true);

    // Length is 10 meters, maximum velocity is 0.5 m/s. -> The weight should be 20 (seconds).
    assertEquals(20.0, edgeEvaluator.computeWeight(reverseEdge, vehicle), 0.0);
  }

  @Test
  void infiniteCostsForUntraversablePaths() {
    Edge edge = new Edge(
        new Path("pathName",
                 new Point("srcPoint").getReference(),
                 new Point("dstPoint").getReference())
            .withLength(10000)
            .withMaxVelocity(0)
            .withMaxReverseVelocity(0),
        false);
    Vehicle vehicle = new Vehicle("someVehicle");

    // Expect the weight/costs to be infinite.
    assertEquals(INFINITE_COSTS, edgeEvaluator.computeWeight(edge, vehicle), 0.0);

    Edge reverseEdge = new Edge(edge.getPath(), true);

    // Expect the weight/costs to be infinite.
    assertEquals(INFINITE_COSTS, edgeEvaluator.computeWeight(reverseEdge, vehicle), 0.0);
  }

}
