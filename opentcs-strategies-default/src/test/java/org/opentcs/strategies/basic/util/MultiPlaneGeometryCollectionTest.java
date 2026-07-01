// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * Tests for {@link MultiPlaneGeometryCollection}.
 */
class MultiPlaneGeometryCollectionTest {

  @Test
  void doesntConsiderIntersectionsAcrossDifferentPlanes() {
    MultiPlaneGeometryCollection collectionA = new MultiPlaneGeometryCollection(
        Map.of(
            3L,
            createCollectionWithOneGeometry(
                new Coordinate(0, 0),
                new Coordinate(0, 10),
                new Coordinate(10, 10),
                new Coordinate(10, 0),
                new Coordinate(0, 0)
            )
        )
    );

    MultiPlaneGeometryCollection collectionB = new MultiPlaneGeometryCollection(
        Map.of(
            7L,
            createCollectionWithOneGeometry(
                new Coordinate(0, 0),
                new Coordinate(0, 10),
                new Coordinate(10, 10),
                new Coordinate(10, 0),
                new Coordinate(0, 0)
            )
        )
    );

    assertThat(collectionA.intersects(collectionB)).isFalse();
    assertThat(collectionB.intersects(collectionA)).isFalse();
  }

  @Test
  void considersIntersectionsOnSamePlanes() {
    MultiPlaneGeometryCollection collectionA = new MultiPlaneGeometryCollection(
        Map.of(
            3L,
            createCollectionWithOneGeometry(
                new Coordinate(0, 0),
                new Coordinate(0, 10),
                new Coordinate(10, 10),
                new Coordinate(10, 0),
                new Coordinate(0, 0)
            )
        )
    );

    MultiPlaneGeometryCollection collectionB = new MultiPlaneGeometryCollection(
        Map.of(
            7L,
            createCollectionWithOneGeometry(
                new Coordinate(0, 0),
                new Coordinate(0, 10),
                new Coordinate(10, 10),
                new Coordinate(10, 0),
                new Coordinate(0, 0)
            ),
            3L,
            createCollectionWithOneGeometry(
                new Coordinate(-5, 0),
                new Coordinate(-5, 10),
                new Coordinate(5, 10),
                new Coordinate(5, 0),
                new Coordinate(-5, 0)
            )
        )
    );

    assertThat(collectionA.intersects(collectionB)).isTrue();
    assertThat(collectionB.intersects(collectionA)).isTrue();
  }

  private GeometryCollection createCollectionWithOneGeometry(Coordinate... coordinates) {
    GeometryFactory geometryFactory = new GeometryFactory();
    return geometryFactory.createGeometryCollection(
        new Geometry[]{geometryFactory.createPolygon(coordinates)}
    );
  }
}
