/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.parking;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.Vehicle;

/**
 * Tests for {@link AbstractParkingPositionSupplier}.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class AbstractParkingPositionSupplierTest {

  private InternalPlantModelService plantModelService;
  private Router router;
  private AbstractParkingPositionSupplierImpl supplier;

  public AbstractParkingPositionSupplierTest() {
  }

  @Before
  public void setUp() {
    plantModelService = mock(InternalPlantModelService.class);
    router = mock(Router.class);
    supplier = new AbstractParkingPositionSupplierImpl(plantModelService, router);

  }

  @Test
  public void returnsEmptyAllParkingPositionsOccupied() {
    Point point1 = new Point("vehicle's current position");
    Point point2 = new Point("parking position occupied by another vehicle")
        .withType(Point.Type.PARK_POSITION)
        .withOccupyingVehicle(new Vehicle("another vehicle").getReference());
    Point point3 = new Point("parking position occupied by yet another vehicle")
        .withType(Point.Type.PARK_POSITION)
        .withOccupyingVehicle(new Vehicle("yet another vehicle").getReference());
    Vehicle vehicle = new Vehicle("vehicle")
        .withCurrentPosition(point1.getReference());

    when(router.getTargetedPoints()).thenReturn(new HashSet<>());
    when(plantModelService.fetchObjects(eq(Point.class), any())).thenReturn(setOf(point2, point3));
    when(plantModelService.expandResources(Collections.singleton(point2.getReference())))
        .thenReturn(Collections.singleton(point2));
    when(plantModelService.expandResources(Collections.singleton(point3.getReference())))
        .thenReturn(Collections.singleton(point3));

    Set<Point> result = supplier.findUsableParkingPositions(vehicle);
    assertTrue(result.isEmpty());
  }

  @Test
  public void returnsUnoccupiedParkingPositions() {
    Point point1 = new Point("vehicle's current position");
    Point point2 = new Point("unoccupied parking position")
        .withType(Point.Type.PARK_POSITION);
    Point point3 = new Point("another unoccupied parking position")
        .withType(Point.Type.PARK_POSITION);
    Vehicle vehicle = new Vehicle("vehicle")
        .withCurrentPosition(point1.getReference());

    when(router.getTargetedPoints()).thenReturn(new HashSet<>());
    when(plantModelService.fetchObjects(eq(Point.class), any())).thenReturn(setOf(point2, point3));
    when(plantModelService.expandResources(Collections.singleton(point2.getReference())))
        .thenReturn(Collections.singleton(point2));
    when(plantModelService.expandResources(Collections.singleton(point3.getReference())))
        .thenReturn(Collections.singleton(point3));

    Set<Point> result = supplier.findUsableParkingPositions(vehicle);
    assertFalse(result.isEmpty());
    assertEquals(setOf(point2, point3), result);
  }

  @Test
  public void returnsExpandedPoints() {
    Point[] points = new Point[5];
    Path[] paths = new Path[points.length];
    for (int i = 0; i < points.length; i++) {
      points[i] = new Point("Point" + i);
      paths[i] = new Path("Path" + i,
                          new Point("some point").getReference(),
                          new Point("some other point").getReference());
    }

    Set<TCSResource<?>> blockMembers = setOf(points[1], points[2], points[3],
                                             paths[0], paths[4]);

    when(plantModelService.fetchObjects(Point.class)).thenReturn(setOf(points));
    when(plantModelService.expandResources(Collections.singleton(points[2].getReference())))
        .thenReturn(blockMembers);

    Set<Point> result = supplier.expandPoints(points[2]);
    assertFalse(result.isEmpty());
    assertEquals(setOf(points[1], points[2], points[3]), result);
  }

  @SuppressWarnings("unchecked")
  private <T> Set<T> setOf(T... resources) {
    return new HashSet<>(Arrays.asList(resources));
  }

  public class AbstractParkingPositionSupplierImpl
      extends AbstractParkingPositionSupplier {

    public AbstractParkingPositionSupplierImpl(InternalPlantModelService plantModelService,
                                               Router router) {
      super(plantModelService, router);
    }

    @Override
    public Optional<Point> findParkingPosition(Vehicle vehicle) {
      throw new UnsupportedOperationException("Outside of this test's scope.");
    }
  }
}
