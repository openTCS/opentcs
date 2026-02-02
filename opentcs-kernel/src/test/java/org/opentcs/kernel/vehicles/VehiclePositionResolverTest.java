// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentcs.components.kernel.services.InternalTCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;

/**
 * Test for {@link VehiclePositionResolver}.
 */
public class VehiclePositionResolverTest {
  private Point point1;

  private Point point2;

  private Point pointLastKnownPosition;

  private InternalTCSObjectService objectService;

  @BeforeEach
  public void setup() {
    point1 = new Point("point_01");
    point1 = point1.withPose(
        point1.getPose().withPosition(new Triple(100, 100, 0))
            .withOrientationAngle(0.0)
    );

    point2 = new Point("point_02");
    point2 = point2.withPose(
        point2.getPose().withPosition(new Triple(200, 200, 0))
            .withOrientationAngle(0.0)
    );

    pointLastKnownPosition = new Point("lastKnownPosition");
    pointLastKnownPosition = pointLastKnownPosition.withPose(
        pointLastKnownPosition.getPose().withPosition(new Triple(300, 300, 0))
            .withOrientationAngle(0.0)
    );

    objectService = mock(InternalTCSObjectService.class);
    setupObjectService(objectService);
  }

  @Test
  void returnLastKnownPositionIfItMatches() {
    VehiclePositionResolver positionResolver = new VehiclePositionResolver(
        objectService, new FixedPositionDeviationPolicy(10, 1)
    );

    assertThat(
        positionResolver.resolveVehiclePosition(
            new Pose(
                new Triple(
                    pointLastKnownPosition.getPose().getPosition().getX() + 7,
                    pointLastKnownPosition.getPose().getPosition().getY() + 7,
                    pointLastKnownPosition.getPose().getPosition().getZ()
                ),
                pointLastKnownPosition.getPose().getOrientationAngle() + 1.0
            ),
            "lastKnownPosition"
        ),
        is(Optional.of("lastKnownPosition"))
    );
  }

  @Test
  void returnMatchingPointIfLastKnownPositionDoesNotMatch() {
    VehiclePositionResolver positionResolver = new VehiclePositionResolver(
        objectService, new FixedPositionDeviationPolicy(10, 1)
    );

    assertThat(
        positionResolver.resolveVehiclePosition(
            new Pose(
                new Triple(
                    point1.getPose().getPosition().getX() + 7,
                    point1.getPose().getPosition().getY() + 7,
                    point1.getPose().getPosition().getZ()
                ),
                point1.getPose().getOrientationAngle() + 1.0
            ),
            "lastKnownPosition"
        ),
        is(Optional.of(point1.getName()))
    );
  }

  @Test
  void returnLastKnownPositionIfNoPointMatches() {
    VehiclePositionResolver positionResolver = new VehiclePositionResolver(
        objectService, new FixedPositionDeviationPolicy(10, 1)
    );

    assertThat(
        positionResolver.resolveVehiclePosition(
            new Pose(
                new Triple(400, 400, 0),
                0.0
            ),
            "lastKnownPosition"
        ),
        is(Optional.of("lastKnownPosition"))
    );
  }

  @Test
  void returnNothingIfNoPointMatchesAndNoLastKnownPosition() {
    VehiclePositionResolver positionResolver = new VehiclePositionResolver(
        objectService, new FixedPositionDeviationPolicy(10, 1)
    );

    assertThat(
        positionResolver.resolveVehiclePosition(
            new Pose(
                new Triple(400, 400, 0),
                0.0
            ),
            null
        ),
        is(Optional.empty())
    );
  }

  @Test
  void returnClosestPointIfMultipleMatch() {
    VehiclePositionResolver positionResolver = new VehiclePositionResolver(
        objectService, new FixedPositionDeviationPolicy(100, 1)
    );

    assertThat(
        "Position is between point1 and point2, but closer to point1",
        positionResolver.resolveVehiclePosition(
            new Pose(
                new Triple(140, 140, 0),
                0.0
            ),
            null
        ),
        is(Optional.of(point1.getName()))
    );
    assertThat(
        "Position is between point1 and point2, but closer to point2",
        positionResolver.resolveVehiclePosition(
            new Pose(
                new Triple(160, 160, 0),
                0.0
            ),
            null
        ),
        is(Optional.of(point2.getName()))
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
  void findPointMatchingPhysicalVehiclePosition(
      int deviationX,
      int deviationY,
      int deviationTheta
  ) {
    setupObjectService(objectService);
    VehiclePositionResolver positionResolver = new VehiclePositionResolver(
        objectService, new FixedPositionDeviationPolicy(2, 15)
    );

    assertThat(
        positionResolver.resolveVehiclePosition(
            new Pose(
                new Triple(
                    point1.getPose().getPosition().getX() + deviationX,
                    point1.getPose().getPosition().getY() + deviationY,
                    point1.getPose().getPosition().getZ()
                ),
                point1.getPose().getOrientationAngle() + deviationTheta
            ),
            null
        ),
        is(Optional.of(point1.getName()))
    );

    assertThat(
        positionResolver.resolveVehiclePosition(
            new Pose(
                new Triple(
                    point1.getPose().getPosition().getX() + deviationX,
                    point1.getPose().getPosition().getY() + deviationY,
                    point1.getPose().getPosition().getZ()
                ),
                point1.getPose().getOrientationAngle() + deviationTheta
            ),
            point1.getName()
        ),
        is(Optional.of(point1.getName()))
    );

    // Even if a last known position was passed to the resolver.
    assertThat(
        positionResolver.resolveVehiclePosition(
            new Pose(
                new Triple(
                    point1.getPose().getPosition().getX() + deviationX,
                    point1.getPose().getPosition().getY() + deviationY,
                    point1.getPose().getPosition().getZ()
                ),
                point1.getPose().getOrientationAngle() + deviationTheta
            ),
            "lastKnownPosition"
        ),
        is(Optional.of(point1.getName()))
    );
  }

  @ParameterizedTest
  @CsvSource({"2,0", "0,2", "3,2"})
  void shouldNotFindPositionIfOutsideDeviationRangeXY(
      int deviationX,
      int deviationY
  ) {
    setupObjectService(objectService);
    VehiclePositionResolver positionResolver = new VehiclePositionResolver(
        objectService, new FixedPositionDeviationPolicy(1, 15)
    );

    assertThat(
        positionResolver.resolveVehiclePosition(
            new Pose(
                new Triple(
                    point1.getPose().getPosition().getX() + deviationX,
                    point1.getPose().getPosition().getY() + deviationY,
                    point1.getPose().getPosition().getZ()
                ),
                point1.getPose().getOrientationAngle()
            ),
            null
        ),
        is(Optional.empty())
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {16, -16})
  void shouldNotFindPositionIfOutsideDeviationRangeTheta(int deviationTheta) {
    setupObjectService(objectService);
    VehiclePositionResolver positionResolver = new VehiclePositionResolver(
        objectService, new FixedPositionDeviationPolicy(1, 15)
    );

    assertThat(
        positionResolver.resolveVehiclePosition(
            new Pose(
                new Triple(
                    point1.getPose().getPosition().getX(),
                    point1.getPose().getPosition().getY(),
                    point1.getPose().getPosition().getZ()
                ),
                point1.getPose().getOrientationAngle() + deviationTheta
            ),
            null
        ),
        is(Optional.empty())
    );
  }

  private void setupObjectService(InternalTCSObjectService objectService) {
    when(objectService.fetch(Point.class, point1.getName())).thenReturn(Optional.of(point1));
    when(objectService.fetch(Point.class, point2.getName())).thenReturn(Optional.of(point2));
    when(objectService.fetch(Point.class, pointLastKnownPosition.getName()))
        .thenReturn(Optional.of(pointLastKnownPosition));

    when(objectService.fetch(Point.class))
        .thenReturn(Set.of(point1, point2, pointLastKnownPosition));

    when(objectService.stream(Point.class))
        .thenAnswer(x -> Stream.of(point1, point2, pointLastKnownPosition));
  }
}
