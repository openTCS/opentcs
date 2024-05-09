/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import java.util.List;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Location.Link;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.routing.ResourceAvoidanceExtractor.ResourcesToAvoid;

/**
 * Tests for {@link ResourceAvoidanceExtractor}.
 */
class ResourceAvoidanceExtractorTest {

  private Point pointA;
  private Point pointB;
  private Point pointC;
  private Path pathAB;
  private Path pathBC;
  private Location locationC;

  private TCSObjectService objectService;
  private ResourceAvoidanceExtractor extractor;

  @BeforeEach
  void setUp() {
    pointA = new Point("Point-A");
    pointB = new Point("Point-B");
    pointC = new Point("Point-C");
    pathAB = new Path("Path-AB", pointA.getReference(), pointB.getReference());
    pathBC = new Path("Path-BC", pointB.getReference(), pointC.getReference());
    locationC = new Location("Location-C", new LocationType("").getReference());
    locationC = locationC.withAttachedLinks(
        Set.of(new Link(locationC.getReference(), pointC.getReference()))
    );

    objectService = mock();
    when(objectService.fetchObject(Point.class, "Point-A")).thenReturn(pointA);
    when(objectService.fetchObject(Point.class, "Point-B")).thenReturn(pointB);
    when(objectService.fetchObject(Point.class, pointC.getReference())).thenReturn(pointC);
    when(objectService.fetchObject(Path.class, "Path-AB")).thenReturn(pathAB);
    when(objectService.fetchObject(Path.class, "Path-BC")).thenReturn(pathBC);
    when(objectService.fetchObject(Location.class, "Location-C")).thenReturn(locationC);

    when(objectService.fetchObject(Point.class, pointA.getReference())).thenReturn(pointA);
    when(objectService.fetchObject(Point.class, pointB.getReference())).thenReturn(pointB);
    when(objectService.fetchObject(Path.class, pathAB.getReference())).thenReturn(pathAB);
    when(objectService.fetchObject(Path.class, pathBC.getReference())).thenReturn(pathBC);
    when(objectService.fetchObject(Location.class, locationC.getReference())).thenReturn(locationC);

    extractor = new ResourceAvoidanceExtractor(objectService);
  }

  @Test
  void shouldReturnEmptyResultForUnknownResourceName() {
    TransportOrder order = new TransportOrder("some-order", List.of())
        .withProperty(ObjectPropConstants.TRANSPORT_ORDER_RESOURCES_TO_AVOID, "unknown-resource");

    ResourcesToAvoid result = extractor.extractResourcesToAvoid(order);

    assertThat(result.getPoints()).isEmpty();
    assertThat(result.getPaths()).isEmpty();
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnEmptyResultForEmptySet() {
    ResourcesToAvoid result = extractor.extractResourcesToAvoid(Set.of());

    assertThat(result.getPoints()).isEmpty();
    assertThat(result.getPaths()).isEmpty();
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldExtractResourcesTransportOrder() {
    TransportOrder order = new TransportOrder("some-order", List.of())
        .withProperty(ObjectPropConstants.TRANSPORT_ORDER_RESOURCES_TO_AVOID,
                      "Point-A,Path-AB,Location-C");

    ResourcesToAvoid result = extractor.extractResourcesToAvoid(order);

    assertThat(result.getPoints())
        .hasSize(2)
        .contains(pointA, pointC);
    assertThat(result.getPaths())
        .hasSize(1)
        .contains(pathAB);
    assertFalse(result.isEmpty());
  }

  @Test
  void shouldExtractResourcesSetOfResources() {
    ResourcesToAvoid result = extractor.extractResourcesToAvoid(
        Set.of(
            pointA.getReference(),
            pathAB.getReference(),
            locationC.getReference()));

    assertThat(result.getPoints())
        .hasSize(2)
        .contains(pointA, pointC);
    assertThat(result.getPaths())
        .hasSize(1)
        .contains(pathAB);
    assertFalse(result.isEmpty());
  }

  @Test
  void shouldNotIgnoreLeadingAndTrailingWhitespace() {
    TransportOrder order = new TransportOrder("some-order", List.of())
        .withProperty(ObjectPropConstants.TRANSPORT_ORDER_RESOURCES_TO_AVOID,
                      "Point-A ,Point-B, Path-AB,Path-BC");

    ResourcesToAvoid result = extractor.extractResourcesToAvoid(order);

    assertThat(result.getPoints())
        .hasSize(1)
        .contains(pointB);
    assertThat(result.getPaths())
        .hasSize(1)
        .contains(pathBC);
    assertFalse(result.isEmpty());
  }
}
