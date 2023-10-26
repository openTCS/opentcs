/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling.modules.areaAllocation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

/**
 * Tests for {@link CustomGeometryFactory}.
 */
public class CustomGeometryFactoryTest {

  private CustomGeometryFactory factory;

  @BeforeEach
  public void setUp() {
    factory = new CustomGeometryFactory();
  }

  @Test
  public void emptyGeometryIsAValidGeometry() {
    assertTrue(CustomGeometryFactory.EMPTY_GEOMETRY.isValid());
  }

  @Test
  public void providesEmptyGeometryWhenCreatingPolygonWithZeroCoordinates() {
    Geometry result = factory.createPolygonOrEmptyGeometry(new Coordinate[]{});
    assertThat(result, is(CustomGeometryFactory.EMPTY_GEOMETRY));
  }

  @Test
  public void providesEmptyGeometryWhenCreatingPolygonWithOneCoordinate() {
    Geometry result = factory.createPolygonOrEmptyGeometry(
        new Coordinate[]{
          new Coordinate(0, 0)
        }
    );
    assertThat(result, is(CustomGeometryFactory.EMPTY_GEOMETRY));
  }

  @Test
  public void providesEmptyGeometryWhenCreatingPolygonWithTwoCoordinates() {
    Geometry result = factory.createPolygonOrEmptyGeometry(
        new Coordinate[]{
          new Coordinate(0, 0),
          new Coordinate(0, 0)
        }
    );
    assertThat(result, is(CustomGeometryFactory.EMPTY_GEOMETRY));
  }

  @Test
  public void providesEmptyGeometryWhenCreatingPolygonWithThreeCoordinates() {
    Geometry result = factory.createPolygonOrEmptyGeometry(
        new Coordinate[]{
          new Coordinate(0, 0),
          new Coordinate(0, 10),
          new Coordinate(0, 0)
        }
    );
    assertThat(result, is(CustomGeometryFactory.EMPTY_GEOMETRY));
  }

  @Test
  public void providesValidPolygonWhenCreatingPolygonWithSufficientAmountOfCoordinates() {
    Geometry result = factory.createPolygonOrEmptyGeometry(
        new Coordinate[]{
          new Coordinate(0, 0),
          new Coordinate(0, 10),
          new Coordinate(5, 5),
          new Coordinate(0, 0)
        });

    assertThat(result, isA(Polygon.class));
    assertTrue(result.isValid());
  }
}
