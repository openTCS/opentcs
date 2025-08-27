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
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Tests for {@link CsmMovementCommandTransformer}.
 */
public class CsmMovementCommandTransformerTest {
  private MovementCommand command;

  @BeforeEach
  public void setUp() {
    command = createSingleStepMovementCommand(
        createPoint(20, 20, 20, -45.0),
        createPoint(30, 30, 30, 45.0),
        createLocation(35, 35, 35)
    );
  }

  @Test
  void applyTranslation() {
    transformAndAssert(
        new CoordinateSystemMapping(10, 20, 30, 0),
        new Pose(new Triple(10, 0, -10), -45.0),
        new Pose(new Triple(20, 10, 0), 45.0),
        new Triple(25, 15, 5)
    );
  }

  @Test
  void applyRotation() {
    transformAndAssert(
        new CoordinateSystemMapping(0, 0, 0, 90),
        new Pose(new Triple(20, -20, 20), -135.0),
        new Pose(new Triple(30, -30, 30), -45.0),
        new Triple(35, -35, 35)
    );
  }

  @Test
  void applyTranslationAndRotation() {
    transformAndAssert(
        new CoordinateSystemMapping(10, 20, 30, 90),
        new Pose(new Triple(0, -10, -10), -135.0),
        new Pose(new Triple(10, -20, 0), -45.0),
        new Triple(15, -25, 5)
    );
  }

  @ParameterizedTest
  @CsvSource({"450.0,30,-30,-45.0", "-405.0,0,42,90.0"})
  void limitTransformedOrientationAngle(
      double rotationZ,
      long expectedDestinationPointX,
      long expectedDestinationPointY,
      double expectedDestinationPointOrientation
  ) {
    CoordinateSystemMapping mapping = new CoordinateSystemMapping(
        0,
        0,
        0,
        rotationZ
    );
    CsmMovementCommandTransformer transformer
        = new CsmMovementCommandTransformer(mapping);

    MovementCommand transformedCommand = transformer.apply(command);

    assertThat(
        transformedCommand.getStep().getDestinationPoint().getPose(),
        is(
            equalTo(
                new Pose(
                    new Triple(expectedDestinationPointX, expectedDestinationPointY, 30),
                    expectedDestinationPointOrientation
                )
            )
        )
    );
    assertThat(
        transformedCommand.getFinalDestination().getPose(),
        is(
            equalTo(
                new Pose(
                    new Triple(expectedDestinationPointX, expectedDestinationPointY, 30),
                    expectedDestinationPointOrientation
                )
            )
        )
    );
  }

  private void transformAndAssert(
      CoordinateSystemMapping mapping,
      Pose transformedSourcePointPose,
      Pose transformedDestinationPointPose,
      Triple transformedLocationPosition
  ) {
    CsmMovementCommandTransformer transformer
        = new CsmMovementCommandTransformer(mapping);

    MovementCommand transformedCommand = transformer.apply(command);

    assertThat(
        transformedCommand.getStep().getSourcePoint().getPose(),
        is(equalTo(transformedSourcePointPose))
    );
    assertThat(
        transformedCommand.getStep().getDestinationPoint().getPose(),
        is(equalTo(transformedDestinationPointPose))
    );
    assertThat(
        transformedCommand.getFinalDestination().getPose(),
        is(equalTo(transformedDestinationPointPose))
    );
    assertThat(
        transformedCommand.getOpLocation().getPosition(),
        is(equalTo(transformedLocationPosition))
    );
    assertThat(
        transformedCommand.getFinalDestinationLocation().getPosition(),
        is(equalTo(transformedLocationPosition))
    );
    // Assert that the drive order is transformed properly
    assertThat(
        transformedCommand.getDriveOrder().getRoute().getSteps().getFirst()
            .getSourcePoint().getPose(),
        is(equalTo(transformedSourcePointPose))
    );
    assertThat(
        transformedCommand.getDriveOrder().getRoute().getSteps().getFirst()
            .getDestinationPoint().getPose(),
        is(equalTo(transformedDestinationPointPose))
    );
    // Assert that the transport order is transformed properly
    assertThat(
        transformedCommand.getTransportOrder().getAllDriveOrders().getFirst()
            .getRoute().getSteps().getFirst().getSourcePoint().getPose(),
        is(equalTo(transformedSourcePointPose))
    );
    assertThat(
        transformedCommand.getTransportOrder().getAllDriveOrders().getFirst()
            .getRoute().getSteps().getFirst().getDestinationPoint().getPose(),
        is(equalTo(transformedDestinationPointPose))
    );
  }

  private Point createPoint(long x, long y, long z, double orientationAngle) {
    return new Point("point")
        .withPose(new Pose(new Triple(x, y, z), orientationAngle));
  }

  private Location createLocation(long x, long y, long z) {
    return new Location("location", new LocationType("location-type").getReference())
        .withPosition(new Triple(x, y, z));
  }

  private MovementCommand createSingleStepMovementCommand(
      Point srcPoint,
      Point destPoint,
      Location destLocation
  ) {
    Route.Step routeStep = new Route.Step(
        new Path("path", srcPoint.getReference(), destPoint.getReference()),
        srcPoint,
        destPoint,
        Vehicle.Orientation.FORWARD,
        0,
        1
    );
    DriveOrder driveOrder = new DriveOrder(
        "drive-order", new DriveOrder.Destination(destLocation.getReference())
    ).withRoute(new Route(List.of(routeStep)));
    return new MovementCommand(
        new TransportOrder("transport-order", List.of(driveOrder)),
        driveOrder,
        routeStep,
        "operation",
        destLocation,
        true,
        destLocation,
        destPoint,
        "operation",
        Map.of()
    );
  }
}
