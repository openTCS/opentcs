// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.Route.Step;

/**
 * Tests for {@link MovementComparisons}.
 */
class MovementComparisonsTest {

  @Test
  void considerIdenticalStepsEqual() {
    List<Step> stepsA = List.of(
        createStep("A", "B", Vehicle.Orientation.FORWARD, 0, 2),
        createStep("B", "C", Vehicle.Orientation.FORWARD, 1, 2)
    );
    List<Step> stepsB = new ArrayList<>(stepsA);

    assertTrue(MovementComparisons.equalsInMovement(stepsA, stepsB));
  }

  @Test
  void considerStepsWithDifferentReroutingTypeEqual() {
    List<Step> stepsA = List.of(
        createStep("A", "B", Vehicle.Orientation.FORWARD, 0, 2),
        createStep("B", "C", Vehicle.Orientation.FORWARD, 1, 2)
    );
    List<Step> stepsB = List.of(
        createStep("A", "B", Vehicle.Orientation.FORWARD, 0, 2),
        createStep("B", "C", Vehicle.Orientation.FORWARD, 1, 2)
            .withReroutingType(ReroutingType.REGULAR)
    );

    assertTrue(MovementComparisons.equalsInMovement(stepsA, stepsB));
  }

  @Test
  void considerStepsWithDifferentExecutionAllowedEqual() {
    List<Step> stepsA = List.of(
        createStep("A", "B", Vehicle.Orientation.FORWARD, 0, 2),
        createStep("B", "C", Vehicle.Orientation.FORWARD, 1, 2)
    );
    List<Step> stepsB = List.of(
        createStep("A", "B", Vehicle.Orientation.FORWARD, 0, 2),
        createStep("B", "C", Vehicle.Orientation.FORWARD, 1, 2)
            .withExecutionAllowed(false)
    );

    assertTrue(MovementComparisons.equalsInMovement(stepsA, stepsB));
  }

  @Test
  void considerStepsWithDifferentCostsEqual() {
    List<Step> stepsA = List.of(
        createStep("A", "B", Vehicle.Orientation.FORWARD, 0, 2),
        createStep("B", "C", Vehicle.Orientation.FORWARD, 1, 2)
    );
    List<Step> stepsB = List.of(
        createStep("A", "B", Vehicle.Orientation.FORWARD, 0, 3),
        createStep("B", "C", Vehicle.Orientation.FORWARD, 1, 3)
    );

    assertTrue(MovementComparisons.equalsInMovement(stepsA, stepsB));
  }

  @Test
  void considerDivergingStepsNotEqual() {
    List<Step> stepsA = List.of(
        createStep("A", "B", Vehicle.Orientation.FORWARD, 0, 2),
        createStep("B", "C", Vehicle.Orientation.FORWARD, 1, 2),
        createStep("C", "D", Vehicle.Orientation.FORWARD, 2, 2),
        createStep("D", "E", Vehicle.Orientation.FORWARD, 3, 2)
    );
    List<Step> stepsB = List.of(
        createStep("A", "B", Vehicle.Orientation.FORWARD, 0, 2),
        createStep("B", "C", Vehicle.Orientation.FORWARD, 1, 2),
        createStep("C", "Y", Vehicle.Orientation.FORWARD, 2, 2),
        createStep("Y", "Z", Vehicle.Orientation.FORWARD, 3, 2)
    );

    assertFalse(MovementComparisons.equalsInMovement(stepsA, stepsB));
  }

  @Test
  void considerStepsWithDifferentPointsNotEqual() {
    List<Step> stepsA = List.of(
        createStep("A", "B", Vehicle.Orientation.FORWARD, 0, 2),
        createStep("B", "C", Vehicle.Orientation.FORWARD, 1, 2)
    );
    List<Step> stepsB = List.of(
        createStep("X", "Y", Vehicle.Orientation.FORWARD, 0, 2),
        createStep("Y", "Z", Vehicle.Orientation.FORWARD, 1, 2)
    );

    assertFalse(MovementComparisons.equalsInMovement(stepsA, stepsB));
  }

  @Test
  void considerStepsWithDifferentOrientationAngleNotEqual() {
    List<Step> stepsA = List.of(
        createStep("A", "B", Vehicle.Orientation.FORWARD, 0, 2),
        createStep("B", "C", Vehicle.Orientation.FORWARD, 1, 2)
    );
    List<Step> stepsB = List.of(
        createStep("A", "B", Vehicle.Orientation.BACKWARD, 0, 2),
        createStep("B", "C", Vehicle.Orientation.BACKWARD, 1, 2)
    );

    assertFalse(MovementComparisons.equalsInMovement(stepsA, stepsB));
  }

  @Test
  void considerStepsWithDifferentRouteIndicesNotEqual() {
    List<Step> stepsA = List.of(
        createStep("A", "B", Vehicle.Orientation.FORWARD, 0, 2),
        createStep("B", "C", Vehicle.Orientation.FORWARD, 1, 2)
    );
    List<Step> stepsB = List.of(
        createStep("A", "B", Vehicle.Orientation.FORWARD, 5, 2),
        createStep("B", "C", Vehicle.Orientation.FORWARD, 6, 2)
    );

    assertFalse(MovementComparisons.equalsInMovement(stepsA, stepsB));
  }

  private Step createStep(
      @Nonnull
      String srcPointName,
      @Nonnull
      String destPointName,
      @Nonnull
      Vehicle.Orientation orientation,
      int routeIndex,
      long cost
  ) {
    requireNonNull(srcPointName, "srcPointName");
    requireNonNull(destPointName, "destPointName");
    requireNonNull(orientation, "orientation");

    Point srcPoint = new Point(srcPointName);
    Point destPoint = new Point(destPointName);
    Path path = new Path(
        srcPointName + "-" + destPointName,
        srcPoint.getReference(),
        destPoint.getReference()
    );

    return new Step(
        path,
        srcPoint,
        destPoint,
        orientation,
        routeIndex,
        cost
    );
  }
}
