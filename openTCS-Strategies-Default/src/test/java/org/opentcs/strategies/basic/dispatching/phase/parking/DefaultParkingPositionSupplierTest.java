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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class DefaultParkingPositionSupplierTest {
  
  private TCSObjectService objectService;
  private Router router;
  private Vehicle mainVehicle;
  private Vehicle vehicle2;
  private Point[] points;
  private Block[] blocks;
  private DefaultParkingPositionSupplier supplier;

  @Before
  public void setUp() {
    objectService = mock(TCSObjectService.class);
    router = mock(Router.class);
    vehicle2 = new Vehicle("vehicle2");
    points = new Point[10];
    supplier = new DefaultParkingPositionSupplier(objectService, router);

  }

  @After
  public void tearDown() {
    supplier.terminate();
  }

  @Test
  public void returnsEmptyForUnknownVehiclePosition() {
    mainVehicle = new Vehicle("mainVehicle")
        .withCurrentPosition(null);
    supplier.initialize();
    Optional<Point> result = supplier.findParkingPosition(mainVehicle);
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void returnsEmptyForUnknownAssignedParkingPosition() {
    mainVehicle = new Vehicle("mainVehicle")
        .withCurrentPosition(new Point("dummyPoint").getReference())
        .withProperty(Dispatcher.PROPKEY_ASSIGNED_PARKING_POSITION, "someUnknownPoint");
    supplier.initialize();
    Optional<Point> result = supplier.findParkingPosition(mainVehicle);
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void returnsEmptyForNoParkingPositionsFromKernel() {
    when(objectService.fetchObjects(Point.class)).thenReturn(new HashSet<>());
    when(objectService.fetchObjects(Block.class)).thenReturn(new HashSet<>());
    mainVehicle = new Vehicle("mainVehicle")
        .withCurrentPosition(new Point("dummyPoint").getReference());
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
      when(objectService.fetchObject(Point.class, points[i].getReference()))
          .thenReturn(points[i]);
    }

    when(objectService.fetchObjects(Point.class))
        .thenReturn(new HashSet<>(Arrays.asList(points)));
    mainVehicle = new Vehicle("mainVehicle")
        .withCurrentPosition(points[3].getReference());
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
    points[1] = new Point("Point" + 1).withType(Point.Type.PARK_POSITION)
        .withOccupyingVehicle(vehicle2.getReference());
    points[4] = new Point("Point" + 4).withType(Point.Type.PARK_POSITION);
    points[5] = new Point("Point" + 5).withType(Point.Type.PARK_POSITION);

    for (int i = 0; i < points.length; i++) {
      when(objectService.fetchObject(Point.class, points[i].getReference()))
          .thenReturn(points[i]);
    }
    when(objectService.fetchObjects(Point.class))
        .thenReturn(new HashSet<>(Arrays.asList(points)));
    mainVehicle = new Vehicle("mainVehicle")
        .withCurrentPosition(points[3].getReference());
    createBlocksAndConfigKernelBlockRequests();
    when(router.getTargetedPoints()).thenReturn(new HashSet<>(Arrays.asList(points[8])));
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
    when(objectService.fetchObjects(Block.class))
        .thenReturn(new HashSet<>(Arrays.asList(blocks)));
    when(objectService.fetchObject(Block.class, blocks[0].getReference())).thenReturn(blocks[0]);
    when(objectService.fetchObject(Block.class, blocks[1].getReference())).thenReturn(blocks[1]);
    when(objectService.fetchObject(Block.class, blocks[2].getReference())).thenReturn(blocks[2]);
  }
}
