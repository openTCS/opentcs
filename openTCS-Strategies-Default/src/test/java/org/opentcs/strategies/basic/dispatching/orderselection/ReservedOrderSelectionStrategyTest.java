/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.orderselection;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class ReservedOrderSelectionStrategyTest {

  private ReservedOrderSelectionStrategy strategy;
  private Vehicle vehicle1;
  private Point vehicle1Point;
  private LocalKernel kernel;
  private Router router;
  private ProcessabilityChecker processabilityChecker;
  private OrderReservationPool orderReservationPool;
  private TransportOrder transportOrder1;
  private TransportOrder transportOrder2;
  private Optional<List<DriveOrder>> optionalDriveOrders;

  @Before
  public void setUp() {
    vehicle1Point = new Point("vehicle1Point");
    vehicle1 = new Vehicle("vehicle1").withCurrentPosition(vehicle1Point.getReference());
    transportOrder1 = createPlainTransportOrder("transportOrder1")
        .withState(TransportOrder.State.DISPATCHABLE);
    transportOrder2 = createPlainTransportOrder("transportOrder2")
        .withState(TransportOrder.State.WITHDRAWN);
    orderReservationPool = mock(OrderReservationPool.class);
    kernel = mock(LocalKernel.class);
    when(kernel.getTCSObject(TransportOrder.class, transportOrder1.getReference()))
        .thenReturn(transportOrder1);
    when(kernel.getTCSObject(TransportOrder.class, transportOrder2.getReference()))
        .thenReturn(transportOrder2);
    processabilityChecker = mock(ProcessabilityChecker.class);
    when(processabilityChecker.checkProcessability(vehicle1, transportOrder1)).thenReturn(true);
    when(kernel.getTCSObject(Point.class, vehicle1Point.getReference())).thenReturn(vehicle1Point);
    router = mock(Router.class);
    List<DriveOrder> driveOrders = createDriveOrders("some driveOrders");
    optionalDriveOrders = Optional.of(driveOrders);
    strategy = new ReservedOrderSelectionStrategy(kernel, router, processabilityChecker, orderReservationPool);
    strategy.initialize();
  }

  @After
  public void tearDown() {
    strategy.terminate();
  }

  @Test
  public void shouldFindReservedOrder() {
    when(router.getRoute(vehicle1, vehicle1Point, transportOrder1)).thenReturn(optionalDriveOrders);
    when(orderReservationPool.findReservations(vehicle1.getReference()))
        .thenReturn(Arrays.asList(transportOrder1.getReference(), transportOrder2.getReference()));
    VehicleOrderSelection strategySelection = strategy.selectOrder(vehicle1);
    assertNotNull(strategySelection);
    assertThat(strategySelection.getVehicle(), is(equalTo(vehicle1)));
    assertThat(strategySelection.getDriveOrders(), is(equalTo(optionalDriveOrders.get())));
    assertThat(strategySelection.getTransportOrder(), is(equalTo(transportOrder1)));
  }

  @Test
  public void shouldFilterReservedOrder() {
    when(router.getRoute(vehicle1, vehicle1Point, transportOrder1)).thenReturn(optionalDriveOrders);
    when(orderReservationPool.findReservations(vehicle1.getReference()))
        .thenReturn(Arrays.asList(transportOrder2.getReference()));
    VehicleOrderSelection strategySelection = strategy.selectOrder(vehicle1);
    assertNull(strategySelection);
  }

  @Test
  public void shouldReturnNullForUnroutableOrder() {
    when(router.getRoute(vehicle1, vehicle1Point, transportOrder1)).thenReturn(Optional.empty());
    when(orderReservationPool.findReservations(vehicle1.getReference()))
        .thenReturn(Arrays.asList(transportOrder1.getReference(), transportOrder2.getReference()));
    VehicleOrderSelection strategySelection = strategy.selectOrder(vehicle1);
    assertNull(strategySelection);
  }

  @Test
  public void returnsNullForUnProcessableOrder() {
    Point vehicle2Point = new Point("vehicle2Point");
    Vehicle vehicle2 = new Vehicle("vehicle2").withCurrentPosition(vehicle2Point.getReference());
    when(processabilityChecker.checkProcessability(vehicle2, transportOrder2)).thenReturn(false);
    when(kernel.getTCSObject(Point.class, vehicle2.getCurrentPosition())).thenReturn(vehicle2Point);
    when(router.getRoute(vehicle2, vehicle2Point, transportOrder1)).thenReturn(optionalDriveOrders);
    when(orderReservationPool.findReservations(vehicle2.getReference()))
        .thenReturn(Arrays.asList(transportOrder1.getReference(), transportOrder2.getReference()));
    VehicleOrderSelection strategySelection = strategy.selectOrder(vehicle2);
    assertNull(strategySelection);
  }

  private TransportOrder createPlainTransportOrder(String transportOrderName) {
    Location destLocation
        = new Location("Some location", new LocationType("Some location type").getReference());
    List<DriveOrder> driveOrders
        = Arrays.asList(new DriveOrder(new DriveOrder.Destination(destLocation.getReference())));
    return new TransportOrder(transportOrderName, driveOrders);
  }

  private List<DriveOrder> createDriveOrders(String destinationName) {
    Location destLocation
        = new Location(destinationName, new LocationType("Some location type").getReference());
    List<DriveOrder> driveOrders
        = Arrays.asList(new DriveOrder(new DriveOrder.Destination(destLocation.getReference())));
    return driveOrders;
  }
}
