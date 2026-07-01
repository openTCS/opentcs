// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;

/**
 * Tests for {@link CustomGeometryFactory}.
 */
class CustomGeometryFactoryTest {

  private CustomGeometryFactory factory;

  @BeforeEach
  void setUp() {
    factory = new CustomGeometryFactory();
  }

  @Test
  void emptyGeometryIsAValidGeometry() {
    assertTrue(CustomGeometryFactory.EMPTY_GEOMETRY.isValid());
  }

  @Test
  void createsGeometricPointFromPlantModelPoint() {
    Point modelPoint = new Point("P1")
        .withPose(new Pose(new Triple(100, 200, 0), 0.0));

    org.locationtech.jts.geom.Point result = factory.createPoint(modelPoint);

    assertThat(result.getX(), is(100.0));
    assertThat(result.getY(), is(200.0));
  }

  @Test
  void providesEmptyGeometryWhenCreatingPolygonFromEnvelopeWithInsufficientAmountOfVertices() {
    Geometry result = factory.createPolygonOrEmptyGeometry(
        new Envelope(List.of(new Couple(0, 0)))
    );

    assertThat(result, is(CustomGeometryFactory.EMPTY_GEOMETRY));
  }

  @Test
  void providesValidPolygonWhenCreatingPolygonFromEnvelopeWithSufficientAmountOfVertices() {
    Geometry result = factory.createPolygonOrEmptyGeometry(
        new Envelope(
            List.of(
                new Couple(0, 0),
                new Couple(0, 10),
                new Couple(5, 5),
                new Couple(0, 0)
            )
        )
    );

    assertThat(result, isA(Polygon.class));
    assertTrue(result.isValid());
  }

  @Test
  void providesEmptyGeometryWhenCreatingPolygonWithZeroCoordinates() {
    Geometry result = factory.createPolygonOrEmptyGeometry(new Coordinate[]{});
    assertThat(result, is(CustomGeometryFactory.EMPTY_GEOMETRY));
  }

  @Test
  void providesEmptyGeometryWhenCreatingPolygonWithOneCoordinate() {
    Geometry result = factory.createPolygonOrEmptyGeometry(new Coordinate(0, 0));

    assertThat(result, is(CustomGeometryFactory.EMPTY_GEOMETRY));
  }

  @Test
  void providesEmptyGeometryWhenCreatingPolygonWithTwoCoordinates() {
    Geometry result = factory.createPolygonOrEmptyGeometry(
        new Coordinate(0, 0),
        new Coordinate(0, 0)
    );

    assertThat(result, is(CustomGeometryFactory.EMPTY_GEOMETRY));
  }

  @Test
  void providesEmptyGeometryWhenCreatingPolygonWithThreeCoordinates() {
    Geometry result = factory.createPolygonOrEmptyGeometry(
        new Coordinate(0, 0),
        new Coordinate(0, 10),
        new Coordinate(0, 0)
    );

    assertThat(result, is(CustomGeometryFactory.EMPTY_GEOMETRY));
  }

  @Test
  void providesValidPolygonWhenCreatingPolygonWithSufficientAmountOfCoordinates() {
    Geometry result = factory.createPolygonOrEmptyGeometry(
        new Coordinate(0, 0),
        new Coordinate(0, 10),
        new Coordinate(5, 5),
        new Coordinate(0, 0)
    );

    assertThat(result, isA(Polygon.class));
    assertTrue(result.isValid());
  }

  @Test
  void providesTransformationForPose() {
    Geometry envelopeGeometry = factory.createPolygonOrEmptyGeometry(
        new Coordinate(0, 0),
        new Coordinate(10, 0),
        new Coordinate(10, 5),
        new Coordinate(0, 5),
        new Coordinate(0, 0)
    );

    AffineTransformation transformation = factory.createTransformationForPose(
        new Pose(
            new Triple(100, 200, 0),
            90.0
        )
    );

    Geometry transformed = transformation.transform(envelopeGeometry);

    assertThat(
        transformed.getCoordinates(),
        arrayContaining(
            new Coordinate(100, 200),
            new Coordinate(100, 210),
            new Coordinate(95, 210),
            new Coordinate(95, 200),
            new Coordinate(100, 200)
        )
    );
  }
}
