// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentcs.data.model.BoundingBox;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PointTO;

/**
 * Tests for {@link PointConverter}.
 */
class PointConverterTest {

  private JsonBinder jsonBinder;
  private PointConverter pointConverter;


  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
    pointConverter = new PointConverter();
  }

  @Test
  void convert() {
    Point point = new Point("point-1");
    point = point
        .withProperties(Map.of("key-1", "value-1"))
        .withPose(new Pose(new Triple(1, 2, 3), 0.5))
        .withType(Point.Type.HALT_POSITION)
        .withIncomingPaths(Set.of(createPath("path-1").getReference()))
        .withOutgoingPaths(Set.of(createPath("path-2").getReference()))
        .withAttachedLinks(
            Set.of(
                new Location.Link(
                    createLocation("location-1").getReference(),
                    point.getReference()
                )
                    .withAllowedOperations(Set.of("operation-1"))
            )
        )
        .withOccupyingVehicle(new Vehicle("vehicle-1").getReference())
        .withVehicleEnvelopes(Map.of("envelope-key-1", new Envelope(List.of(new Couple(4, 5)))))
        .withMaxVehicleBoundingBox(
            new BoundingBox(1000, 2000, 3000)
                .withReferenceOffset(new Couple(6, 7))
        )
        .withLayout(new Point.Layout(new Couple(8, 9), 10));

    PointTO result = pointConverter.convert(point);

    Approvals.verify(jsonBinder.toJson(result));
  }

  @ParameterizedTest
  @EnumSource(Point.Type.class)
  void convertsTypes(Point.Type type) {
    Point point = new Point("point-1").withType(type);

    PointTO result = pointConverter.convert(point);

    PointTO.TypeTO expectedType = switch (type) {
      case HALT_POSITION -> PointTO.TypeTO.HALT_POSITION;
      case PARK_POSITION -> PointTO.TypeTO.PARK_POSITION;
    };
    assertThat(result.getType()).isEqualTo(expectedType);
  }

  @Test
  void convertsNullMembersToNull() {
    Point point = new Point("point-1")
        .withOccupyingVehicle(null);

    PointTO result = pointConverter.convert(point);

    assertThat(result.getOccupyingVehicle()).isNull();
  }

  @Test
  void convertsOrientationAngleNaNToNull() {
    Point point = new Point("point-1").withPose(new Pose(new Triple(0, 0, 0), Double.NaN));

    PointTO result = pointConverter.convert(point);

    assertThat(result.getPose().getOrientationAngle()).isNull();
  }

  private Path createPath(String name) {
    return new Path(
        name,
        new Point("dummy-source").getReference(),
        new Point("dummy-destination").getReference()
    );
  }

  private Location createLocation(String name) {
    return new Location(name, new LocationType("dummy-type").getReference());
  }
}
