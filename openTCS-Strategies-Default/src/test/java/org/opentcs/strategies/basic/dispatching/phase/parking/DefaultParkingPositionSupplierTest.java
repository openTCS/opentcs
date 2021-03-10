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
import static org.hamcrest.Matchers.is;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class DefaultParkingPositionSupplierTest {

  private InternalPlantModelService plantModelService;
  private Router router;
  private Vehicle vehicle;
  private DefaultParkingPositionSupplier supplier;

  @Before
  public void setUp() {
    plantModelService = mock(InternalPlantModelService.class);
    router = mock(Router.class);
    vehicle = new Vehicle("vehicle");
    supplier = new DefaultParkingPositionSupplier(plantModelService, router);
  }

  @After
  public void tearDown() {
    supplier.terminate();
  }

  @Test
  public void returnsEmptyForUnknownVehiclePosition() {
    vehicle = vehicle.withCurrentPosition(null);
    supplier.initialize();
    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void returnsEmptyForUnknownAssignedParkingPosition() {
    vehicle = vehicle
        .withCurrentPosition(new Point("dummyPoint").getReference())
        .withProperty(Dispatcher.PROPKEY_ASSIGNED_PARKING_POSITION, "someUnknownPoint");
    supplier.initialize();
    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void returnsEmptyForNoParkingPositionsFromKernel() {
    when(plantModelService.fetchObjects(Point.class)).thenReturn(new HashSet<>());
    when(plantModelService.fetchObjects(Block.class)).thenReturn(new HashSet<>());
    vehicle = vehicle.withCurrentPosition(new Point("dummyPoint").getReference());
    supplier.initialize();
    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void returnsClosestParkingPosition() {
    Point point1 = new Point("vehicle's current position");
    Point point2 = new Point("parking position")
        .withType(Point.Type.PARK_POSITION);
    Point point3 = new Point("another parking position closer to the vehicle")
        .withType(Point.Type.PARK_POSITION);
    vehicle = new Vehicle("vehicle").withCurrentPosition(point1.getReference());

    when(router.getTargetedPoints()).thenReturn(new HashSet<>());
    when(plantModelService.fetchObject(Point.class, point1.getReference())).thenReturn(point1);
    when(plantModelService.fetchObjects(eq(Point.class), any())).thenReturn(setOf(point2, point3));
    when(router.getCosts(vehicle, point1, point2)).thenReturn(10L);
    when(router.getCosts(vehicle, point1, point3)).thenReturn(1L);

    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertTrue("expected a parking position to be present", result.isPresent());
    assertEquals(point3, result.get());
  }

  @SuppressWarnings("unchecked")
  private <T> Set<T> setOf(T... resources) {
    return new HashSet<>(Arrays.asList(resources));
  }
}
