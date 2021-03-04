/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.recharging;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import org.junit.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder.Destination;

/**
 * Tests for {@link DefaultRechargePositionSupplier}.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public class DefaultRechargePositionSupplierTest {

  /**
   * The object service used by the recharge strategy.
   */
  private TCSObjectService objectService;
  /**
   * The router used by the recharge strategy.
   */
  private Router router;
  /**
   * The supplier instance to be tested.
   */
  private DefaultRechargePositionSupplier rechargePosSupplier;

  @Before
  public void setUp() {
    objectService = mock(TCSObjectService.class);
    router = mock(Router.class);
    rechargePosSupplier = new DefaultRechargePositionSupplier(objectService, router);
  }

  @After
  public void tearDown() {
    rechargePosSupplier.terminate();
  }

  @Test
  public void findExistingRechargePosition() {
    Point currentVehiclePoint = new Point("Current vehicle point")
        .withType(Point.Type.HALT_POSITION);
    Vehicle vehicle = new Vehicle("Some vehicle")
        .withRechargeOperation("Do some recharging")
        .withCurrentPosition(currentVehiclePoint.getReference());

    Point locationAccessPoint = new Point("Location access point")
        .withType(Point.Type.HALT_POSITION);

    LocationType rechargeLocType = new LocationType("Recharge location type")
        .withAllowedOperations(Collections.singletonList(vehicle.getRechargeOperation()));
    Location location = new Location("Recharge location", rechargeLocType.getReference());

    Location.Link link = new Location.Link(location.getReference(),
                                           locationAccessPoint.getReference());
    location = location.withAttachedLinks(new HashSet<>(Arrays.asList(link)));

    locationAccessPoint = locationAccessPoint.withAttachedLinks(new HashSet<>(Arrays.asList(link)));

    when(objectService.fetchObjects(Location.class))
        .thenReturn(Collections.singleton(location));
    when(objectService.fetchObject(LocationType.class, rechargeLocType.getReference()))
        .thenReturn(rechargeLocType);
    when(objectService.fetchObject(Point.class, currentVehiclePoint.getReference()))
        .thenReturn(currentVehiclePoint);
    when(objectService.fetchObject(Point.class, locationAccessPoint.getReference()))
        .thenReturn(locationAccessPoint);

    rechargePosSupplier.initialize();

    List<Destination> result = rechargePosSupplier.findRechargeSequence(vehicle);
    assertNotNull(result);
    assertThat(result, is(not(empty())));
  }

  @Test
  public void returnsEmptyListIfNoRechargePositionExists() {
    Point currentVehiclePoint = new Point("Current vehicle point");
    Vehicle vehicle = new Vehicle("Some vehicle")
        .withCurrentPosition(currentVehiclePoint.getReference());

    when(objectService.fetchObject(Point.class, currentVehiclePoint.getReference()))
        .thenReturn(currentVehiclePoint);

    rechargePosSupplier.initialize();

    List<Destination> result = rechargePosSupplier.findRechargeSequence(vehicle);
    assertNotNull(result);
    assertThat(result, is(empty()));
  }

  @Test
  public void returnsEmptyListIfAssignedRechargePositionDoesNotExist() {
    Point currentVehiclePoint = new Point("Current vehicle point");
    Vehicle vehicle = new Vehicle("Some vehicle")
        .withRechargeOperation("Do some recharging")
        .withCurrentPosition(currentVehiclePoint.getReference())
        .withProperty(Dispatcher.PROPKEY_ASSIGNED_RECHARGE_LOCATION, "someUnknownLocation");

    Point locationAccessPoint = new Point("Location access point")
        .withType(Point.Type.HALT_POSITION);

    LocationType rechargeLocType = new LocationType("Recharge location type")
        .withAllowedOperations(Arrays.asList("Recharge"));
    Location location = new Location("Recharge location", rechargeLocType.getReference());

    Location.Link link = new Location.Link(location.getReference(),
                                           locationAccessPoint.getReference());
    location = location.withAttachedLinks(new HashSet<>(Arrays.asList(link)));

    locationAccessPoint = locationAccessPoint.withAttachedLinks(new HashSet<>(Arrays.asList(link)));

    when(objectService.fetchObjects(Location.class))
        .thenReturn(Collections.singleton(location));
    when(objectService.fetchObject(LocationType.class, rechargeLocType.getReference()))
        .thenReturn(rechargeLocType);
    when(objectService.fetchObject(Point.class, currentVehiclePoint.getReference()))
        .thenReturn(currentVehiclePoint);
    when(objectService.fetchObject(Point.class, locationAccessPoint.getReference()))
        .thenReturn(locationAccessPoint);

    rechargePosSupplier.initialize();

    List<Destination> result = rechargePosSupplier.findRechargeSequence(vehicle);
    assertNotNull(result);
    assertThat(result, is(empty()));
  }

}
