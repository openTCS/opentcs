// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;

class DistanceInAdvanceControllerTest {

  private DistanceInAdvanceController controller;

  @BeforeEach
  public void setUp() {
    controller = new DistanceInAdvanceController(5000L);
  }

  @Test
  public void shouldAcceptCommandsWhileLessThanMaxDistanceInAdvance() {
    assertTrue(
        controller.canAcceptNextCommand(
            List.of(
                createCommandWithLength(2000),
                createCommandWithLength(1000)
            )
        )
    );
  }

  @Test
  public void shouldNotAcceptCommandsWhileGreaterThanMaxDistanceInAdvance() {
    assertFalse(
        controller.canAcceptNextCommand(
            List.of(
                createCommandWithLength(4000),
                createCommandWithLength(2000)
            )
        )
    );
  }

  private MovementCommand createCommandWithLength(long length) {
    Point srcPoint = new Point("1");
    Point destPoint = new Point("2");
    Path path = new Path("1 -- 2", srcPoint.getReference(), destPoint.getReference())
        .withLength(length);
    Route.Step step = new Route.Step(
        path,
        srcPoint,
        destPoint,
        Vehicle.Orientation.FORWARD,
        0,
        1
    );

    return new MovementCommand(
        new TransportOrder("1", List.of()),
        new DriveOrder(
            "drive-order",
            new DriveOrder.Destination(new Point("point1").getReference())
        ),
        step,
        "NOP",
        null,
        true,
        null,
        new Point("point2"),
        "NOP",
        Map.of()
    );
  }
}
