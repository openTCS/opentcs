/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.parking;

import java.util.Arrays;
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
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Tests for {@link PrioritizedParkingPositionSupplier}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PrioritizedParkingPositionSupplierTest {

  private PrioritizedParkingPositionSupplier supplier;
  private InternalPlantModelService plantModelService;
  private Router router;
  private ParkingPositionToPriorityFunction priorityFunction;

  public PrioritizedParkingPositionSupplierTest() {
  }

  @Before
  public void setUp() {
    plantModelService = mock(InternalPlantModelService.class);
    router = mock(Router.class);
    priorityFunction = new ParkingPositionToPriorityFunction();
    supplier = new PrioritizedParkingPositionSupplier(plantModelService, router, priorityFunction);
  }

  @Test
  public void returnsPrioritizedParkingPosition() {
    Point point1 = new Point("vehicle's current position");
    Point point2 = new Point("parking position without priority")
        .withType(Point.Type.PARK_POSITION);
    Point point3 = new Point("parking position with priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "1");
    Vehicle vehicle = new Vehicle("vehicle")
        .withCurrentPosition(point1.getReference());

    when(router.getTargetedPoints()).thenReturn(new HashSet<>());
    when(plantModelService.fetchObject(Point.class, point1.getReference())).thenReturn(point1);
    when(plantModelService.fetchObjects(eq(Point.class), any())).thenReturn(setOf(point2, point3));
    when(router.getCosts(vehicle, point1, point3)).thenReturn(1L);

    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertTrue("expected a prioritized parking position to be present", result.isPresent());
    assertEquals(point3, result.get());
  }

  @Test
  public void returnsClosestPrioritizedParkingPositionForPositionsWithSamePriority() {
    Point point1 = new Point("vehicle's current position");
    Point point2 = new Point("parking position with some priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "4");
    Point point3 = new Point("parking position with the same priority but closer to the vehicle")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "4");
    Vehicle vehicle = new Vehicle("vehicle")
        .withCurrentPosition(point1.getReference());

    when(router.getTargetedPoints()).thenReturn(new HashSet<>());
    when(plantModelService.fetchObject(Point.class, point1.getReference())).thenReturn(point1);
    when(plantModelService.fetchObjects(eq(Point.class), any())).thenReturn(setOf(point2, point3));
    when(router.getCosts(vehicle, point1, point2)).thenReturn(10L);
    when(router.getCosts(vehicle, point1, point3)).thenReturn(1L);

    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertTrue("expected a prioritized parking position to be present", result.isPresent());
    assertEquals(point3, result.get());
  }

  @Test
  public void returnsHigherPrioritizedParkingPositionThanCurrentlyOccupying() {
    Point point1 = new Point("vehicle's current parking position with some priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "17");
    Point point2 = new Point("parking position with lower priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "20");
    Point point3 = new Point("parking position with higher priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "12");
    Vehicle vehicle = new Vehicle("vehicle")
        .withCurrentPosition(point1.getReference());

    when(router.getTargetedPoints()).thenReturn(new HashSet<>());
    when(plantModelService.fetchObject(Point.class, point1.getReference())).thenReturn(point1);
    when(plantModelService.fetchObjects(eq(Point.class), any())).thenReturn(setOf(point2, point3));
    when(router.getCosts(vehicle, point1, point3)).thenReturn(1L);

    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertTrue("expected a prioritized parking position to be present", result.isPresent());
    assertEquals(point3, result.get());
  }

  @Test
  public void returnsEmptyForOnlyPositionsAvailableWithSamePriorityAsCurrentlyOccupying() {
    Point point1 = new Point("vehicle's current parking position with some priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "13");
    Point point2 = new Point("parking position with the same priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "13");
    Point point3 = new Point("another parking position with the same priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "13");
    Vehicle vehicle = new Vehicle("vehicle")
        .withCurrentPosition(point1.getReference());

    when(router.getTargetedPoints()).thenReturn(new HashSet<>());
    when(plantModelService.fetchObject(Point.class, point1.getReference())).thenReturn(point1);
    when(plantModelService.fetchObjects(eq(Point.class), any())).thenReturn(setOf(point2, point3));

    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertFalse("expected no prioritized parking position to be present", result.isPresent());
  }

  @Test
  public void returnsEmptyForNoHigherPrioritizedParkingPositionsAvailable() {
    Point point1 = new Point("vehicle's current parking position with some priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "13");
    Point point2 = new Point("parking position with lower priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "24");
    Point point3 = new Point("another parking position with lower priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "45");
    Vehicle vehicle = new Vehicle("vehicle")
        .withCurrentPosition(point1.getReference());

    when(router.getTargetedPoints()).thenReturn(new HashSet<>());
    when(plantModelService.fetchObject(Point.class, point1.getReference())).thenReturn(point1);
    when(plantModelService.fetchObjects(eq(Point.class), any())).thenReturn(setOf(point2, point3));

    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertFalse("expected no prioritized parking position to be present", result.isPresent());
  }

  @Test
  public void returnsEmptyForNoPrioritizedParkingPositionAvailable() {
    Point point1 = new Point("vehicle's current position");
    Point point2 = new Point("parking position with without priority")
        .withType(Point.Type.PARK_POSITION);
    Point point3 = new Point("another parking position without priority")
        .withType(Point.Type.PARK_POSITION);
    Vehicle vehicle = new Vehicle("vehicle")
        .withCurrentPosition(point1.getReference());

    when(router.getTargetedPoints()).thenReturn(new HashSet<>());
    when(plantModelService.fetchObject(Point.class, point1.getReference())).thenReturn(point1);
    when(plantModelService.fetchObjects(eq(Point.class), any())).thenReturn(setOf(point2, point3));

    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertFalse("expected no prioritized parking position to be present", result.isPresent());
  }

  @SuppressWarnings("unchecked")
  private <T> Set<T> setOf(T... resources) {
    return new HashSet<>(Arrays.asList(resources));
  }
}
