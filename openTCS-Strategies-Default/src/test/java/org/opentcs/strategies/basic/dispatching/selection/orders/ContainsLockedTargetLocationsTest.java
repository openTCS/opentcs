/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.selection.orders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.ArgumentMatchers;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;

/**
 * Defines test cases for {@link ContainsLockedTargetLocations}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ContainsLockedTargetLocationsTest {

  /**
   * The class to test.
   */
  private ContainsLockedTargetLocations filter;
  /**
   * The object service to use.
   */
  private TCSObjectService objectService;
  /**
   * The local object pool to be used by the object service.
   */
  private Map<TCSObjectReference<?>, TCSObject<?>> localObjectPool;

  @Before
  public void setUp() {
    localObjectPool = new HashMap<>();
    objectService = mock(TCSObjectService.class);
    when(objectService.fetchObject(any(), ArgumentMatchers.<TCSObjectReference<?>>any()))
        .thenAnswer(invocation -> localObjectPool.get((TCSObjectReference<?>) invocation.getArgument(1)));
    filter = new ContainsLockedTargetLocations(objectService);
  }

  @Test
  public void shouldFilterTransportOrderWithLockedLocation() {
    Collection<String> result = filter.apply(transportOrderWithLockedLocation());
    assertFalse(result.isEmpty());
  }

  @Test
  public void shouldIgnoreTransportOrderWithUnlockedLocation() {
    Collection<String> result = filter.apply(transportOrderWithoutLockedLocation());
    assertTrue(result.isEmpty());
  }

  @Test
  public void shouldIgnoreTransportOrderWithPointDestination() {
    Collection<String> result = filter.apply(transportOrderWithPointDestination());
    assertTrue(result.isEmpty());
  }

  private TransportOrder transportOrderWithLockedLocation() {
    List<DriveOrder> driveOrders = new ArrayList<>();
    LocationType locationType = new LocationType("LocationType-1");

    Location location = new Location("Location-1", locationType.getReference());
    localObjectPool.put(location.getReference(), location);
    DriveOrder.Destination destination = new DriveOrder.Destination(location.getReference());
    driveOrders.add(new DriveOrder(destination));

    location = new Location("Location-2", locationType.getReference()).withLocked(true);
    localObjectPool.put(location.getReference(), location);
    destination = new DriveOrder.Destination(location.getReference());
    driveOrders.add(new DriveOrder(destination));

    return new TransportOrder("TransportOrder-1", driveOrders);
  }

  private TransportOrder transportOrderWithoutLockedLocation() {
    List<DriveOrder> driveOrders = new ArrayList<>();
    LocationType locationType = new LocationType("LocationType-1");

    Location location = new Location("Location-1", locationType.getReference());
    localObjectPool.put(location.getReference(), location);
    DriveOrder.Destination destination = new DriveOrder.Destination(location.getReference());
    driveOrders.add(new DriveOrder(destination));

    location = new Location("Location-2", locationType.getReference());
    localObjectPool.put(location.getReference(), location);
    destination = new DriveOrder.Destination(location.getReference());
    driveOrders.add(new DriveOrder(destination));

    return new TransportOrder("TransportOrder-1", driveOrders);
  }

  private TransportOrder transportOrderWithPointDestination() {
    List<DriveOrder> driveOrders = new ArrayList<>();

    Point point = new Point("Point-1");
    localObjectPool.put(point.getReference(), point);
    DriveOrder.Destination destination = new DriveOrder.Destination(point.getReference());
    driveOrders.add(new DriveOrder(destination));

    return new TransportOrder("TransportOrder-1", driveOrders);
  }
}
