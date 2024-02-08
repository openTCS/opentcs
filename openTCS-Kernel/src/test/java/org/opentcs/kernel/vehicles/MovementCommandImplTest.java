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
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.Route.Step;

/**
 * Test for {@link MovementCommandImpl}.
 */
public class MovementCommandImplTest {

  private Location location;

  @BeforeEach
  public void setUp() {
    location = new Location("L1", new LocationType("LT1").getReference());
  }

  @Test
  void considerIdenticalMovementCommandsEqual() {
    Step stepAB = createStep("A", "B",
                             Vehicle.Orientation.FORWARD,
                             0, true, null);
    Route route = new Route(List.of(stepAB), 22);

    MovementCommandImpl command = new MovementCommandImpl(route,
                                                          stepAB,
                                                          "some-operation",
                                                          location,
                                                          false,
                                                          location,
                                                          new Point("p1"),
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
    MovementCommandImpl commandA = new MovementCommandImpl(route,
                                                           stepAB,
                                                           "some-operation",
                                                           location,
                                                           false,
                                                           location,
                                                           new Point("p1"),
                                                           "final-operation",
                                                           Map.of("a", "b"));

    Step stepBC = createStep("B", "C",
                             Vehicle.Orientation.FORWARD,
                             0, true, null);
    Route route2 = new Route(List.of(stepBC), 22);
    MovementCommandImpl commandB = new MovementCommandImpl(route2,
                                                           stepBC,
                                                           "some-operation",
                                                           location,
                                                           false,
                                                           location,
                                                           new Point("p1"),
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
    MovementCommandImpl commandA = new MovementCommandImpl(route,
                                                           stepAB,
                                                           "operation-a",
                                                           location,
                                                           false,
                                                           location,
                                                           new Point("p1"),
                                                           "final-operation",
                                                           Map.of("a", "b"));

    MovementCommandImpl commandB = new MovementCommandImpl(route,
                                                           stepAB,
                                                           "operation-b",
                                                           location,
                                                           false,
                                                           location,
                                                           new Point("p1"),
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
