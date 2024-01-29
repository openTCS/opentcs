/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling.modules.areaAllocation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryCollection;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;

/**
 * Tests for {@link CachingAreaProvider}.
 */
class CachingAreaProviderTest {

  private CachingAreaProvider areaProvider;
  private TCSObjectService objectService;
  private Point point1;
  private Point point2;
  private Point point3;
  private Path path1;
  private Path path2;

  @BeforeEach
  void setUp() {
    objectService = mock();
    areaProvider = new CachingAreaProvider(objectService);

    point1 = new Point("point1");
    point2 = new Point("point2");
    point3 = new Point("point3");
    path1 = new Path("path1", point1.getReference(), point2.getReference());
    path2 = new Path("path2", point2.getReference(), point3.getReference());
  }

  @Test
  void providesEmptyGeometryCollectionWhenCacheIsEmpty() {
    // Arrange
    areaProvider.initialize();

    // Act & Assert: When not providing a resource set
    GeometryCollection result = areaProvider.getAreas("some-envelope-key", Set.of());
    assertThat(result.getNumGeometries(), is(0));
    assertTrue(result.isEmpty());

    // Act & Assert: When providing a resource set
    result = areaProvider.getAreas("some-envelope-key", Set.of(point1, point2, path1));
    assertThat(result.getNumGeometries(), is(0));
    assertTrue(result.isEmpty());
  }

  @Test
  void providesNonEmptyGeometryCollectionForKnownEnvelopeKey() {
    // Arrange
    point2 = point2.withVehicleEnvelopes(
        Map.of(
            "some-envelope-key",
            new Envelope(
                List.of(new Couple(100, 0),
                        new Couple(100, 10),
                        new Couple(110, 10),
                        new Couple(110, 0),
                        new Couple(100, 0))
            )
        )
    );
    point3 = point3.withVehicleEnvelopes(
        Map.of(
            "some-envelope-key",
            new Envelope(
                List.of(new Couple(110, 0),
                        new Couple(110, 10),
                        new Couple(120, 10),
                        new Couple(120, 0),
                        new Couple(110, 0))
            )
        )
    );
    path1 = path1.withVehicleEnvelopes(
        Map.of(
            "some-envelope-key",
            new Envelope(
                List.of(new Couple(0, 0),
                        new Couple(0, 10),
                        new Couple(110, 10),
                        new Couple(110, 0),
                        new Couple(0, 0))
            )
        )
    );
    when(objectService.fetchObjects(eq(Point.class), any()))
        .thenReturn(Set.of(point1, point2, point3));
    when(objectService.fetchObjects(eq(Path.class), any()))
        .thenReturn(Set.of(path1, path2));
    areaProvider.initialize();

    // Act & Assert: Three resources with envelopes
    GeometryCollection result = areaProvider.getAreas(
        "some-envelope-key", Set.of(point2, path1, point3, path2)
    );
    assertThat(result.getNumGeometries(), is(3));

    // Act & Assert: Only one resources with envelopes
    result = areaProvider.getAreas(
        "some-envelope-key", Set.of(point3, path2)
    );
    assertThat(result.getNumGeometries(), is(1));
  }

  @Test
  void providesEmptyGeometryCollectionForUnknownEnvelopeKey() {
    // Arrange
    point2 = point2.withVehicleEnvelopes(
        Map.of(
            "some-envelope-key",
            new Envelope(
                List.of(new Couple(100, 0),
                        new Couple(100, 10),
                        new Couple(110, 10),
                        new Couple(110, 0),
                        new Couple(100, 0))
            )
        )
    );
    point3 = point3.withVehicleEnvelopes(
        Map.of(
            "some-envelope-key",
            new Envelope(
                List.of(new Couple(110, 0),
                        new Couple(110, 10),
                        new Couple(120, 10),
                        new Couple(120, 0),
                        new Couple(110, 0))
            )
        )
    );
    path1 = path1.withVehicleEnvelopes(
        Map.of(
            "some-envelope-key",
            new Envelope(
                List.of(new Couple(0, 0),
                        new Couple(0, 10),
                        new Couple(110, 10),
                        new Couple(110, 0),
                        new Couple(0, 0))
            )
        )
    );
    when(objectService.fetchObjects(eq(Point.class), any()))
        .thenReturn(Set.of(point1, point2, point3));
    when(objectService.fetchObjects(eq(Path.class), any()))
        .thenReturn(Set.of(path1, path2));
    areaProvider.initialize();

    // Act & Assert
    GeometryCollection result = areaProvider.getAreas(
        "some-unknown-envelope-key", Set.of(point2, path1, point3, path2)
    );
    assertThat(result.getNumGeometries(), is(0));
    assertTrue(result.isEmpty());
  }
}
