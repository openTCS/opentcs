/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.Route.Step;
import org.opentcs.data.order.TransportOrder;

/**
 * Test for {@link MovementCommandImpl}.
 */
public class MovementCommandImplTest {

  private Point destinationPoint;
  private Location location;

  @BeforeEach
  public void setUp() {
    destinationPoint = new Point("p1");
    location = new Location("L1", new LocationType("LT1").getReference());
  }

  @Test
  void considerIdenticalMovementCommandsEqual() {
    Step stepAB = createStep("A", "B",
                             Vehicle.Orientation.FORWARD,
                             0, true, null);
    Route route = new Route(List.of(stepAB), 22);

    DriveOrder driveOrder
        = new DriveOrder(new DriveOrder.Destination(destinationPoint.getReference()))
            .withRoute(route);
    TransportOrder transportOrder = new TransportOrder("some-order", List.of(driveOrder));

    MovementCommandImpl command = new MovementCommandImpl(transportOrder,
                                                          driveOrder,
                                                          stepAB,
                                                          "some-operation",
                                                          location,
                                                          false,
                                                          location,
                                                          destinationPoint,
                                                          "final-operation",
                                                          Map.of());

    assertTrue(command.equalsInMovement(command));
  }

  @Test
  void considerMovementCommandsWithDifferentStepsNotEqual() {
    Step stepAB = createStep("A", "B",
                             Vehicle.Orientation.FORWARD,
                             0, true, null);
    Route route = new Route(List.of(stepAB), 22);

    DriveOrder driveOrder
        = new DriveOrder(new DriveOrder.Destination(destinationPoint.getReference()))
            .withRoute(route);
    TransportOrder transportOrder = new TransportOrder("some-order", List.of(driveOrder));

    MovementCommandImpl commandA = new MovementCommandImpl(transportOrder,
                                                           driveOrder,
                                                           stepAB,
                                                           "some-operation",
                                                           location,
                                                           false,
                                                           location,
                                                           destinationPoint,
                                                           "final-operation",
                                                           Map.of("a", "b"));

    Step stepBC = createStep("B", "C",
                             Vehicle.Orientation.FORWARD,
                             0, true, null);
    Route route2 = new Route(List.of(stepBC), 22);

    driveOrder
        = new DriveOrder(new DriveOrder.Destination(destinationPoint.getReference()))
            .withRoute(route2);
    transportOrder = new TransportOrder("some-order", List.of(driveOrder));

    MovementCommandImpl commandB = new MovementCommandImpl(transportOrder,
                                                           driveOrder,
                                                           stepBC,
                                                           "some-operation",
                                                           location,
                                                           false,
                                                           location,
                                                           destinationPoint,
                                                           "final-operation",
                                                           Map.of("a", "b"));

    assertFalse(commandA.equalsInMovement(commandB));
  }

  @Test
  void considerMovementCommandsWithOperationNotEqual() {
    Step stepAB = createStep("A", "B",
                             Vehicle.Orientation.FORWARD,
                             0, true, null);
    Route route = new Route(List.of(stepAB), 22);

    DriveOrder driveOrder
        = new DriveOrder(new DriveOrder.Destination(destinationPoint.getReference()))
            .withRoute(route);
    TransportOrder transportOrder = new TransportOrder("some-order", List.of(driveOrder));

    MovementCommandImpl commandA = new MovementCommandImpl(transportOrder,
                                                           driveOrder,
                                                           stepAB,
                                                           "operation-a",
                                                           location,
                                                           false,
                                                           location,
                                                           destinationPoint,
                                                           "final-operation",
                                                           Map.of("a", "b"));

    MovementCommandImpl commandB = new MovementCommandImpl(transportOrder,
                                                           driveOrder,
                                                           stepAB,
                                                           "operation-b",
                                                           location,
                                                           false,
                                                           location,
                                                           destinationPoint,
                                                           "final-operation",
                                                           Map.of("a", "b"));

    assertFalse(commandA.equalsInMovement(commandB));
  }

  private Route.Step createStep(@Nonnull String srcPointName,
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

    return new Route.Step(path,
                          srcPoint,
                          destPoint,
                          orientation,
                          routeIndex,
                          executionAllowed,
                          reroutingType);
  }
}
