/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.parking;

import static java.lang.Math.abs;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class DefaultParkingPositionSupplierTest {

  private InternalPlantModelService plantModelService;
  private Router router;
  private Vehicle mainVehicle;
  private Vehicle vehicle2;
  private Point[] points;
  private Block[] blocks;
  private DefaultParkingPositionSupplier supplier;

  @Before
  public void setUp() {
    plantModelService = mock(InternalPlantModelService.class);
    router = mock(Router.class);
    vehicle2 = new Vehicle("vehicle2");
    mainVehicle = new Vehicle("mainVehicle");
    points = new Point[10];
    supplier = new DefaultParkingPositionSupplier(plantModelService, router);

  }

  @After
  public void tearDown() {
    supplier.terminate();
  }

  @Test
  public void returnsEmptyForUnknownVehiclePosition() {
    mainVehicle = mainVehicle.withCurrentPosition(null);
    supplier.initialize();
    Optional<Point> result = supplier.findParkingPosition(mainVehicle);
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void returnsEmptyForUnknownAssignedParkingPosition() {
    mainVehicle = mainVehicle
        .withCurrentPosition(new Point("dummyPoint").getReference())
        .withProperty(Dispatcher.PROPKEY_ASSIGNED_PARKING_POSITION, "someUnknownPoint");
    supplier.initialize();
    Optional<Point> result = supplier.findParkingPosition(mainVehicle);
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void returnsEmptyForNoParkingPositionsFromKernel() {
    when(plantModelService.fetchObjects(Point.class)).thenReturn(new HashSet<>());
    when(plantModelService.fetchObjects(Block.class)).thenReturn(new HashSet<>());
    mainVehicle = mainVehicle.withCurrentPosition(new Point("dummyPoint").getReference());
    supplier.initialize();
    Optional<Point> result = supplier.findParkingPosition(mainVehicle);
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void returnsEmptyForAllBlocksReserved() {
    //all points that are not parking positions are occupied by "vehicle2".
    //every third point is a parking position, all other points are halt positions.
    for (int i = 0; i < points.length; i++) {
      if (i % 3 == 0) {
        points[i] = new Point("Point" + i).withType(Point.Type.PARK_POSITION);
      }
      else {
        points[i] = new Point("Point" + i).withType(Point.Type.HALT_POSITION)
            .withOccupyingVehicle(vehicle2.getReference());
      }
      when(plantModelService.fetchObject(Point.class, points[i].getReference()))
          .thenReturn(points[i]);
    }

    when(plantModelService.fetchObjects(Point.class)).thenReturn(setOf(points));
    mainVehicle = mainVehicle.withCurrentPosition(points[3].getReference());
    createBlocksAndConfigKernelBlockRequests();
    for (int i = 0; i < points.length; i++) {
      when(router.getCosts(mainVehicle, points[3], points[i])).thenReturn((long) abs(i - 3 + 1));
    }
    supplier.initialize();
    Optional<Point> result = supplier.findParkingPosition(mainVehicle);
    assertThat(result, is(notNullValue()));
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void returnsClosestParkingPosition() {
    for (int i = 0; i < points.length; i++) {
      points[i] = new Point("Point" + i);
    }
    points[1] = points[1].withType(Point.Type.PARK_POSITION)
        .withOccupyingVehicle(vehicle2.getReference());
    points[4] = points[4].withType(Point.Type.PARK_POSITION);
    points[5] = points[5].withType(Point.Type.PARK_POSITION);

    for (int i = 0; i < points.length; i++) {
      when(plantModelService.fetchObject(Point.class, points[i].getReference()))
          .thenReturn(points[i]);
    }
    when(plantModelService.fetchObjects(eq(Point.class), any()))
        .thenReturn(setOf(points[1], points[4], points[5]));
    mainVehicle = mainVehicle.withCurrentPosition(points[3].getReference());
    createBlocksAndConfigKernelBlockRequests();
    when(router.getTargetedPoints()).thenReturn(setOf(points[8]));
    for (int i = 0; i < points.length; i++) {
      when(router.getCosts(mainVehicle, points[3], points[i])).thenReturn((long) (abs(i - 3)));
    }
    supplier.initialize();
    Optional<Point> result = supplier.findParkingPosition(mainVehicle);
    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getName(), is(equalTo("Point5")));

  }

  private void createBlocksAndConfigKernelBlockRequests() {
    Set<TCSResourceReference<?>> pointRefSet1 = new HashSet<>();
    Set<TCSResourceReference<?>> pointRefSet2 = new HashSet<>();
    Set<TCSResourceReference<?>> pointRefSet3 = new HashSet<>();
    for (int i = 0; i < 5; i++) {
      pointRefSet1.add(points[i].getReference());
    }
    for (int i = 2; i < 7; i++) {
      pointRefSet2.add(points[i].getReference());
    }
    for (int i = 7; i < 10; i++) {
      pointRefSet3.add(points[i].getReference());
    }
    blocks = new Block[3];
    blocks[0] = new Block("Block1").withMembers(pointRefSet1);
    blocks[1] = new Block("Block2").withMembers(pointRefSet2);
    blocks[2] = new Block("Block3").withMembers(pointRefSet3);
    when(plantModelService.fetchObjects(Block.class))
        .thenReturn(new HashSet<>(Arrays.asList(blocks)));
    when(plantModelService.fetchObject(Block.class, blocks[0].getReference())).thenReturn(blocks[0]);
    when(plantModelService.fetchObject(Block.class, blocks[1].getReference())).thenReturn(blocks[1]);
    when(plantModelService.fetchObject(Block.class, blocks[2].getReference())).thenReturn(blocks[2]);

    when(plantModelService.expandResources(Collections.singleton(points[1].getReference())))
        .thenReturn(setOf(points[0], points[1], points[2], points[3], points[4]));
    when(plantModelService.expandResources(Collections.singleton(points[4].getReference())))
        .thenReturn(setOf(points[0], points[1], points[2], points[3], points[4], points[5], points[6]));
    when(plantModelService.expandResources(Collections.singleton(points[5].getReference())))
        .thenReturn(setOf(points[2], points[3], points[4], points[5], points[6]));
  }

  @SuppressWarnings("unchecked")
  private <T> Set<T> setOf(T... resources) {
    return new HashSet<>(Arrays.asList(resources));
  }
}
