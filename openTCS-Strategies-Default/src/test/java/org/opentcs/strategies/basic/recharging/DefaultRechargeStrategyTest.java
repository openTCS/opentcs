/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.recharging;

import java.util.Collections;
import java.util.HashSet;
import org.junit.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.mockito.Matchers;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.orderselection.recharging.DefaultRechargePositionSupplier;

/**
 * Test for the SimpleRechargeStrategy.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public class DefaultRechargeStrategyTest {

  /**
   * The kernel used by the recharge strategy.
   */
  private LocalKernel kernel;
  /**
   * The router used by the recharge strategy.
   */
  private Router router;

  @Before
  public void setUp() {
    // Initialize final members
    kernel = mock(LocalKernel.class);
    router = mock(Router.class);
  }

  /**
   * getRechargeLocation() should return a recharge location if there
   * are one or more that can be reached by the vehicle.
   */
//  @Ignore("Does not work (throws NullPointerException). Probably related to the"
//      + " problem that SimpleRechargeStrategy depends on a Kernel instance")
  @Test
  public void testNotNullWhenARechargeLocationInModel() {
    // Vehicle
    Vehicle vehicle = new Vehicle(30, "Vehicle");

    // Points
    Point point = new Point(1, "A point");
    point.setType(Point.Type.HALT_POSITION);
    Point accessPoint = new Point(2, "An access point");
    accessPoint.setType(Point.Type.HALT_POSITION);

    // Paths
    Path path = new Path(10, "Connecting path", point.getReference(), accessPoint.getReference());

    // Location
    LocationType locType = new LocationType(20, "Recharge location type");
    locType.addAllowedOperation(vehicle.getRechargeOperation());
    Location location = new Location(21, "Recharge location", locType.getReference());

    // Connections
    Location.Link link = new Location.Link(location.getReference(), accessPoint.getReference());
    location.attachLink(link);
    accessPoint.attachLink(link);

    vehicle.setCurrentPosition(point.getReference());

    when(kernel.getTCSObjects(Block.class))
        .thenReturn(new HashSet<Block>());
    when(kernel.getTCSObjects(Location.class))
        .thenReturn(Collections.singleton(location));
    when(kernel.getTCSObject(LocationType.class, locType.getReference()))
        .thenReturn(locType);
    when(kernel.getTCSObject(Point.class, point.getReference()))
        .thenReturn(point);
    when(kernel.getTCSObject(Point.class, accessPoint.getReference()))
        .thenReturn(accessPoint);
    when(router.getTargetedPoints())
        .thenReturn(new HashSet<Point>());

    DefaultRechargePositionSupplier strategy = new DefaultRechargePositionSupplier(kernel, router);
    strategy.initialize();

    assertFalse(strategy.findRechargeSequence(vehicle).isEmpty());
    
    strategy.terminate();
  }

  /**
   * getRechargeLocation() should return null if there is no recharge location
   * in the model at all.
   */
  @Test
  public void testNullWhenNoRechargeLocationInModel() {
    // Preparation
    Vehicle vehicle = new Vehicle(0, "Vehicle");
    Point point = new Point(1, "Point");
    vehicle.setCurrentPosition(point.getReference());

    // Stubs for methods used by SimpleRechargeStrategy in this test
    when(kernel.getTCSObject(Point.class, point.getReference()))
        .thenReturn(point);
    when(kernel.getTCSObjects(Location.class))
        .thenReturn(new HashSet<Location>());
    when(kernel.getTCSObjects(Block.class))
        .thenReturn(new HashSet<Block>());
    when(router.getTargetedPoints())
        .thenReturn(new HashSet<Point>());

    DefaultRechargePositionSupplier strategy = new DefaultRechargePositionSupplier(kernel, router);
    strategy.initialize();

    assertTrue(strategy.findRechargeSequence(vehicle).isEmpty());

    // Verify that un-mocked methods do not get called
    verify(router, never()).getCostsByPointRef(
        any(Vehicle.class),
        Matchers.<TCSObjectReference<Point>>any(),
        Matchers.<TCSObjectReference<Point>>any());
    verify(kernel, times(1)).getTCSObject(
        eq(Point.class),
        Matchers.<TCSObjectReference<Point>>any());
    
    strategy.terminate();
  }
}
