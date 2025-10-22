// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import static java.lang.Math.toRadians;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;

/**
 * Test for {@link VehiclePositionResolver}.
 */
public class VehiclePositionResolverTest {
  private VehiclePositionResolver positionResolver;

  private Point point1;

  private Point point2;

  private Point pointLastKnownPosition;

  private TCSObjectService objectService;

  private VehiclePositionResolverConfiguration configuration;

  @BeforeEach
  public void setup() {
    point1 = new Point("point_01");
    point1 = point1.withPose(point1.getPose().withPosition(new Triple(200, 100, 0)));

    point2 = new Point("point_02");
    point2 = point2.withPose(point2.getPose().withPosition(new Triple(123, 456, 0)));

    pointLastKnownPosition = new Point("lastKnownPosition");
    pointLastKnownPosition = pointLastKnownPosition
        .withPose(pointLastKnownPosition.getPose().withPosition(new Triple(200, 300, 0)));

    objectService = mock(TCSObjectService.class);
    setupObjectService(objectService);
    configuration = mock(VehiclePositionResolverConfiguration.class);
    when(configuration.deviationXY()).thenReturn(0);
    when(configuration.deviationTheta()).thenReturn(0);
    positionResolver = new VehiclePositionResolver(objectService, configuration);
  }

  private void setupObjectService(TCSObjectService objectService) {
    when(objectService.fetch(Point.class, point1.getReference())).thenReturn(Optional.of(point1));
    when(objectService.fetch(Point.class, point1.getName())).thenReturn(Optional.of(point1));
    when(objectService.fetch(Point.class, point2.getReference())).thenReturn(Optional.of(point2));
    when(objectService.fetch(Point.class, point2.getName())).thenReturn(Optional.of(point2));
    when(objectService.fetch(Point.class, pointLastKnownPosition.getReference()))
        .thenReturn(Optional.of(pointLastKnownPosition));
    when(objectService.fetch(Point.class, pointLastKnownPosition.getName()))
        .thenReturn(Optional.of(pointLastKnownPosition));
    Set<Point> points = Set.of(point1, point2, pointLastKnownPosition);
    when(objectService.fetch(Point.class)).thenReturn(points);
  }

  @Test
  public void useLastKnownPositionAsAFallback() {
    assertThat(
        positionResolver.resolveVehiclePosition(
            new Pose(new Triple(200, 300, 0), 0),
            "lastKnownPosition"
        ),
        is("lastKnownPosition")
    );

    // Even if the last known position is null
    assertThat(
        positionResolver.resolveVehiclePosition(
            new Pose(new Triple(400, 400, 400), 0),
            null
        ),
        is(nullValue())
    );
  }

  @ParameterizedTest
  @CsvSource(
    {" 0,  0,   0",
        " 1,  0,   0",
        "-1, -0,   0",
        " 0,  1,   0",
        "-0, -1,   0",
        " 0,  0,  14",
        " 0,  0, -14",
        " 1,  1,  14",
        "-1, -1, -14"}
  )
  public void findPointMatchingPhysicalVehiclePosition(
      int deviationX,
      int deviationY,
      int deviationTheta
  ) {
    point1 = point1.withPose(point1.getPose().withOrientationAngle(0));
    setupObjectService(objectService);
    when(configuration.deviationXY()).thenReturn(2);
    when(configuration.deviationTheta()).thenReturn(15);

    assertThat(
        positionResolver.resolveVehiclePosition(
            new Pose(
                new Triple(
                    point1.getPose().getPosition().getX() + deviationX,
                    point1.getPose().getPosition().getY() + deviationY,
                    0
                ),
                toRadians(deviationTheta)
            ),
            null
        ),
        is(point1.getName())
    );

    assertThat(
        positionResolver.resolveVehiclePosition(
            new Pose(
                new Triple(
                    Math.round(point1.getPose().getPosition().getX() + deviationX),
                    Math.round(point1.getPose().getPosition().getY() + deviationY),
                    0
                ),
                toRadians(deviationTheta)
            ),
            point1.getName()
        ),
        is(point1.getName())
    );

    // Even if a last known position was passed to the resolver.
    assertThat(
        positionResolver.resolveVehiclePosition(
            new Pose(
                new Triple(
                    Math.round(point1.getPose().getPosition().getX() + deviationX),
                    Math.round(point1.getPose().getPosition().getY() + deviationY),
                    0
                ),
                toRadians(deviationTheta)
            ),
            "lastKnownPosition"
        ),
        is(point1.getName())
    );
  }

  @ParameterizedTest
  @CsvSource({"2,0", "0,2", "3,2"})
  public void shouldNotFindPositionIfOutsideDeviationRangeXY(
      int deviationX,
      int deviationY
  ) {
    point1 = point1.withPose(point1.getPose().withOrientationAngle(0));
    setupObjectService(objectService);
    when(configuration.deviationXY()).thenReturn(1);
    when(configuration.deviationTheta()).thenReturn(15);

    assertThat(
        positionResolver.resolveVehiclePosition(
            new Pose(
                new Triple(
                    point1.getPose().getPosition().getX() + deviationX,
                    point1.getPose().getPosition().getY() + deviationY,
                    0
                ),
                0.0
            ),
            null
        ),
        is(nullValue())
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {16, -16})
  public void shouldNotFindPositionIfOutsideDeviationRangeTheta(int deviationTheta) {
    point1 = point1.withPose(point1.getPose().withOrientationAngle(0));
    setupObjectService(objectService);
    when(configuration.deviationXY()).thenReturn(1);
    when(configuration.deviationTheta()).thenReturn(15);

    assertThat(
        positionResolver.resolveVehiclePosition(
            new Pose(
                new Triple(
                    point1.getPose().getPosition().getX(),
                    point1.getPose().getPosition().getY(),
                    0
                ),
                toRadians(deviationTheta)
            ),
            null
        ),
        is(nullValue())
    );
  }
}
