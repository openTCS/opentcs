// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;

/**
 * Test for {@link MovementCommand}.
 */
public class MovementCommandTest {

  private Point destinationPoint;
  private Location location;

  @BeforeEach
  public void setUp() {
    destinationPoint = new Point("p1");
    location = new Location("L1", new LocationType("LT1").getReference());
  }

  @Test
  void considerIdenticalMovementCommandsEqual() {
    Route.Step stepAB = createStep("A", "B", Vehicle.Orientation.FORWARD, 0, 22);
    Route route = new Route(List.of(stepAB));

    DriveOrder driveOrder
        = new DriveOrder(
            "some-order",
            new DriveOrder.Destination(destinationPoint.getReference())
        )
            .withRoute(route);
    TransportOrder transportOrder = new TransportOrder("some-order", List.of(driveOrder));

    MovementCommand command = new MovementCommand(
        transportOrder,
        driveOrder,
        stepAB,
        "some-operation",
        location,
        false,
        location,
        destinationPoint,
        "final-operation",
        Map.of()
    );

    assertTrue(command.equalsInMovement(command));
  }

  @Test
  void considerMovementCommandsWithDifferentStepsNotEqual() {
    Route.Step stepAB = createStep("A", "B", Vehicle.Orientation.FORWARD, 0, 22);
    Route route = new Route(List.of(stepAB));

    DriveOrder driveOrder
        = new DriveOrder(
            "some-order",
            new DriveOrder.Destination(destinationPoint.getReference())
        )
            .withRoute(route);
    TransportOrder transportOrder = new TransportOrder("some-order", List.of(driveOrder));

    MovementCommand commandA = new MovementCommand(
        transportOrder,
        driveOrder,
        stepAB,
        "some-operation",
        location,
        false,
        location,
        destinationPoint,
        "final-operation",
        Map.of("a", "b")
    );

    Route.Step stepBC = createStep("B", "C", Vehicle.Orientation.FORWARD, 0, 22);
    Route route2 = new Route(List.of(stepBC));

    driveOrder
        = new DriveOrder(
            "some-order",
            new DriveOrder.Destination(destinationPoint.getReference())
        )
            .withRoute(route2);
    transportOrder = new TransportOrder("some-order", List.of(driveOrder));

    MovementCommand commandB = new MovementCommand(
        transportOrder,
        driveOrder,
        stepBC,
        "some-operation",
        location,
        false,
        location,
        destinationPoint,
        "final-operation",
        Map.of("a", "b")
    );

    assertFalse(commandA.equalsInMovement(commandB));
  }

  @Test
  void considerMovementCommandsWithOperationNotEqual() {
    Route.Step stepAB = createStep("A", "B", Vehicle.Orientation.FORWARD, 0, 22);
    Route route = new Route(List.of(stepAB));

    DriveOrder driveOrder
        = new DriveOrder(
            "some-order",
            new DriveOrder.Destination(destinationPoint.getReference())
        )
            .withRoute(route);
    TransportOrder transportOrder = new TransportOrder("some-order", List.of(driveOrder));

    MovementCommand commandA = new MovementCommand(
        transportOrder,
        driveOrder,
        stepAB,
        "operation-a",
        location,
        false,
        location,
        destinationPoint,
        "final-operation",
        Map.of("a", "b")
    );

    MovementCommand commandB = new MovementCommand(
        transportOrder,
        driveOrder,
        stepAB,
        "operation-b",
        location,
        false,
        location,
        destinationPoint,
        "final-operation",
        Map.of("a", "b")
    );

    assertFalse(commandA.equalsInMovement(commandB));
  }

  private Route.Step createStep(
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

    return new Route.Step(
        path,
        srcPoint,
        destPoint,
        orientation,
        routeIndex,
        cost
    );
  }
}
