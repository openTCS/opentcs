// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles.transformers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Tests for {@link CoordinateSystemMovementCommandTransformer}.
 */
@Deprecated
public class CoordinateSystemMovementCommandTransformerTest {

  private MovementCommand command;

  @BeforeEach
  public void setUp() {
    Point pointDestDriveOrder = new Point("P1");
    Point pointDestStep = new Point("P2")
        .withPose(new Pose(new Triple(20, 20, 20), -45));
    Point finalDest = new Point("P3")
        .withPose(new Pose(new Triple(30, 30, 30), 45));
    Location locationDest = new Location("L1", new LocationType("LT1").getReference())
        .withPosition(new Triple(35, 35, 35));
    command = new MovementCommand(
        new TransportOrder("T1", List.of()),
        new DriveOrder("some-order", new Destination(pointDestDriveOrder.getReference())),
        new Route.Step(
            null,
            null,
            pointDestStep,
            Vehicle.Orientation.FORWARD,
            1,
            10
        ),
        MovementCommand.MOVE_OPERATION,
        null,
        false,
        locationDest,
        finalDest,
        MovementCommand.PARK_OPERATION,
        Map.of()
    );
  }

  @Test
  void applyTransformation() {
    CoordinateSystemTransformation transformation = new CoordinateSystemTransformation(
        10,
        20,
        30,
        40
    );
    CoordinateSystemMovementCommandTransformer transformer
        = new CoordinateSystemMovementCommandTransformer(transformation);

    MovementCommand transformedCommand = transformer.apply(command);

    assertThat(
        transformedCommand.getStep().getDestinationPoint().getPose(),
        is(equalTo(new Pose(new Triple(30, 40, 50), -5.0)))
    );
    assertThat(
        transformedCommand.getFinalDestination().getPose(),
        is(equalTo(new Pose(new Triple(40, 50, 60), 85.0)))
    );
    assertThat(
        transformedCommand.getFinalDestinationLocation().getPosition(),
        is(equalTo(new Triple(45, 55, 65)))
    );
  }

  @ParameterizedTest
  @CsvSource({"420.0,15.0,105.0", "-470.0,-155,-65.0"})
  void limitTransformedOrientationAngle(
      double offsetOrientation,
      double expectedDestinationPointOrientation,
      double expectedFinalDestinationOrientation
  ) {
    CoordinateSystemTransformation transformation = new CoordinateSystemTransformation(
        0,
        0,
        0,
        offsetOrientation
    );
    CoordinateSystemMovementCommandTransformer transformer
        = new CoordinateSystemMovementCommandTransformer(transformation);

    MovementCommand transformedCommand = transformer.apply(command);

    assertThat(
        transformedCommand.getStep().getDestinationPoint().getPose(),
        is(equalTo(new Pose(new Triple(20, 20, 20), expectedDestinationPointOrientation)))
    );
    assertThat(
        transformedCommand.getFinalDestination().getPose(),
        is(equalTo(new Pose(new Triple(30, 30, 30), expectedFinalDestinationOrientation)))
    );
  }
}
