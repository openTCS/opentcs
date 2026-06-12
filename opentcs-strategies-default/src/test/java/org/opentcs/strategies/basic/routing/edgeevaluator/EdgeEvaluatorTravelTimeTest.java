// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.edgeevaluator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;
import static org.opentcs.strategies.basic.routing.PointRouter.INFINITE_COSTS;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Unit tests for {@link EdgeEvaluatorTravelTime}.
 */
class EdgeEvaluatorTravelTimeTest {

  private Vehicle vehicle;
  private EdgeEvaluatorTravelTime edgeEvaluator;

  @BeforeEach
  void setUp() {
    vehicle = new Vehicle("someVehicle")
        .withMaxVelocity(1_000)
        .withMaxReverseVelocity(1_000);
    edgeEvaluator = new EdgeEvaluatorTravelTime();
  }

  @Test
  void computeTravelTime() {
    Edge edge = new Edge(
        new Path(
            "pathName",
            new Point("srcPoint").getReference(),
            new Point("dstPoint").getReference()
        )
            .withLength(10_000)
            .withMaxVelocity(1_000)
            .withMaxReverseVelocity(500),
        false
    );

    // Length is 10 meters, maximum velocity is 1 m/s. -> The weight should be 10 (seconds).
    assertThat(edgeEvaluator.computeWeight(edge, vehicle))
        .isEqualTo(10.0, withPrecision(0.0));

    Edge reverseEdge = new Edge(edge.getPath(), true);

    // Length is 10 meters, maximum velocity is 0.5 m/s. -> The weight should be 20 (seconds).
    assertThat(edgeEvaluator.computeWeight(reverseEdge, vehicle))
        .isEqualTo(20.0, withPrecision(0.0));
  }

  @Test
  void computeNonzeroCostsForHighSpeedOnShortPath() {
    Edge edge = new Edge(
        new Path(
            "pathName",
            new Point("srcPoint").getReference(),
            new Point("dstPoint").getReference()
        )
            .withLength(400)
            .withMaxVelocity(1_000)
            .withMaxReverseVelocity(800),
        false
    );

    assertThat(edgeEvaluator.computeWeight(edge, vehicle))
        .isGreaterThan(0.0);

    Edge reverseEdge = new Edge(edge.getPath(), true);

    assertThat(edgeEvaluator.computeWeight(reverseEdge, vehicle))
        .isGreaterThan(0.0);
  }

  @Test
  void infiniteCostsForUntraversablePaths() {
    Edge edge = new Edge(
        new Path(
            "pathName",
            new Point("srcPoint").getReference(),
            new Point("dstPoint").getReference()
        )
            .withLength(10000)
            .withMaxVelocity(0)
            .withMaxReverseVelocity(0),
        false
    );

    // Expect the weight/costs to be infinite.
    assertThat(edgeEvaluator.computeWeight(edge, vehicle))
        .isEqualTo(INFINITE_COSTS, withPrecision(0.0));

    Edge reverseEdge = new Edge(edge.getPath(), true);

    // Expect the weight/costs to be infinite.
    assertThat(edgeEvaluator.computeWeight(reverseEdge, vehicle))
        .isEqualTo(INFINITE_COSTS, withPrecision(0.0));
  }

}
