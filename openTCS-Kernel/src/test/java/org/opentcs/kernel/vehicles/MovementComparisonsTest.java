/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.Route.Step;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Tests for {@link MovementComparisons}.
 */
class MovementComparisonsTest {

  @Test
  void considerIdenticalStepsEqual() {
    List<Step> stepsA = List.of(createStep("A", "B", Vehicle.Orientation.FORWARD, 0, true, null),
                                createStep("B", "C", Vehicle.Orientation.FORWARD, 1, true, null));
    List<Step> stepsB = new ArrayList<>(stepsA);

    assertTrue(MovementComparisons.equalsInMovement(stepsA, stepsB));
  }

  @Test
  void considerStepsWithDifferentReroutingTypeEqual() {
    List<Step> stepsA = List.of(createStep("A", "B", Vehicle.Orientation.FORWARD, 0, true, null),
                                createStep("B", "C", Vehicle.Orientation.FORWARD, 1, true, null));
    List<Step> stepsB = List.of(
        createStep("A", "B", Vehicle.Orientation.FORWARD, 0, true, null),
        createStep("B", "C", Vehicle.Orientation.FORWARD, 1, true, ReroutingType.REGULAR)
    );

    assertTrue(MovementComparisons.equalsInMovement(stepsA, stepsB));
  }

  @Test
  void considerStepsWithDifferentExecutionAllowedEqual() {
    List<Step> stepsA = List.of(createStep("A", "B", Vehicle.Orientation.FORWARD, 0, true, null),
                                createStep("B", "C", Vehicle.Orientation.FORWARD, 1, true, null));
    List<Step> stepsB = List.of(createStep("A", "B", Vehicle.Orientation.FORWARD, 0, true, null),
                                createStep("B", "C", Vehicle.Orientation.FORWARD, 1, false, null));

    assertTrue(MovementComparisons.equalsInMovement(stepsA, stepsB));
  }

  @Test
  void considerDivergingStepsNotEqual() {
    List<Step> stepsA = List.of(createStep("A", "B", Vehicle.Orientation.FORWARD, 0, true, null),
                                createStep("B", "C", Vehicle.Orientation.FORWARD, 1, true, null),
                                createStep("C", "D", Vehicle.Orientation.FORWARD, 2, true, null),
                                createStep("D", "E", Vehicle.Orientation.FORWARD, 3, true, null));
    List<Step> stepsB = List.of(createStep("A", "B", Vehicle.Orientation.FORWARD, 0, true, null),
                                createStep("B", "C", Vehicle.Orientation.FORWARD, 1, true, null),
                                createStep("C", "Y", Vehicle.Orientation.FORWARD, 2, true, null),
                                createStep("Y", "Z", Vehicle.Orientation.FORWARD, 3, true, null));

    assertFalse(MovementComparisons.equalsInMovement(stepsA, stepsB));
  }

  @Test
  void considerStepsWithDifferentPointsNotEqual() {
    List<Step> stepsA = List.of(createStep("A", "B", Vehicle.Orientation.FORWARD, 0, true, null),
                                createStep("B", "C", Vehicle.Orientation.FORWARD, 1, true, null));
    List<Step> stepsB = List.of(createStep("X", "Y", Vehicle.Orientation.FORWARD, 0, true, null),
                                createStep("Y", "Z", Vehicle.Orientation.FORWARD, 1, true, null));

    assertFalse(MovementComparisons.equalsInMovement(stepsA, stepsB));
  }

  @Test
  void considerStepsWithDifferentOrientationAngleNotEqual() {
    List<Step> stepsA = List.of(createStep("A", "B", Vehicle.Orientation.FORWARD, 0, true, null),
                                createStep("B", "C", Vehicle.Orientation.FORWARD, 1, true, null));
    List<Step> stepsB = List.of(createStep("A", "B", Vehicle.Orientation.BACKWARD, 0, true, null),
                                createStep("B", "C", Vehicle.Orientation.BACKWARD, 1, true, null));

    assertFalse(MovementComparisons.equalsInMovement(stepsA, stepsB));
  }

  @Test
  void considerStepsWithDifferentRouteIndicesNotEqual() {
    List<Step> stepsA = List.of(createStep("A", "B", Vehicle.Orientation.FORWARD, 0, true, null),
                                createStep("B", "C", Vehicle.Orientation.FORWARD, 1, true, null));
    List<Step> stepsB = List.of(createStep("A", "B", Vehicle.Orientation.FORWARD, 5, true, null),
                                createStep("B", "C", Vehicle.Orientation.FORWARD, 6, true, null));

    assertFalse(MovementComparisons.equalsInMovement(stepsA, stepsB));
  }

  private Step createStep(@Nonnull String srcPointName,
                          @Nonnull String destPointName,
                          @Nonnull Vehicle.Orientation orientation,
                          int routeIndex,
                          boolean executionAllowed,
                          @Nullable ReroutingType reroutingType) {
    requireNonNull(srcPointName, "srcPointName");
    requireNonNull(destPointName, "destPointName");
    requireNonNull(orientation, "orientation");

    Point srcPoint = new Point(srcPointName);
    Point destPoint = new Point(destPointName);
    Path path = new Path(srcPointName + "-" + destPointName,
                         srcPoint.getReference(),
                         destPoint.getReference());

    return new Step(path,
                    srcPoint,
                    destPoint,
                    orientation,
                    routeIndex,
                    executionAllowed,
                    reroutingType);
  }

  private static class TestMovementCommand
      implements MovementCommand {

    private final Step step;
    private final String operation;

    TestMovementCommand(Step step, String operation) {
      this.step = requireNonNull(step, "step");
      this.operation = requireNonNull(operation, "operation");
    }

    @Override
    public TransportOrder getTransportOrder() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DriveOrder getDriveOrder() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Step getStep() {
      return step;
    }

    @Override
    public String getOperation() {
      return operation;
    }

    @Override
    @Deprecated
    public boolean isWithoutOperation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasEmptyOperation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Location getOpLocation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isFinalMovement() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Point getFinalDestination() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Location getFinalDestinationLocation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getFinalOperation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equalsInMovement(MovementCommand command) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, String> getProperties() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

  }
}
