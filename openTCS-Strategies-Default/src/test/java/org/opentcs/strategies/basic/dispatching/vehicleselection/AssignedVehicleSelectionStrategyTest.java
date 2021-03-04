/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.vehicleselection;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AssignedVehicleSelectionStrategyTest {

  private LocalKernel kernel;

  private Router router;

  private ProcessabilityChecker processabilityChecker;

  private AssignedVehicleSelectionStrategy strategy;

  private Point vehiclePosition;

  private Vehicle vehicle;

  @Before
  public void setUp() {
    kernel = mock(LocalKernel.class);
    router = mock(Router.class);
    processabilityChecker = mock(ProcessabilityChecker.class);

    vehiclePosition = new Point("Current vehicle point");
    vehicle = new Vehicle("Some vehicle")
        .withCurrentPosition(vehiclePosition.getReference());
    
    when(kernel.getTCSObject(Point.class, vehiclePosition.getReference()))
        .thenReturn(vehiclePosition);
    when(kernel.getTCSObject(Vehicle.class, vehicle.getReference()))
        .thenReturn(vehicle);

    strategy = new AssignedVehicleSelectionStrategy(kernel, router, processabilityChecker);
  }

  @After
  public void tearDown() {
    strategy.terminate();
  }

  @Test
  public void returnNullForOrderWithoutSequenceOrIntendedVehicle() {
    TransportOrder order = createPlainTransportOrder()
        .withWrappingSequence(null)
        .withIntendedVehicle(null);

    strategy.initialize();

    assertNull(strategy.selectVehicle(order));
  }

  @Test
  public void returnUnassignableSelectionForUnavailableIntendedVehicleForOrder() {
    TransportOrder order = createPlainTransportOrder()
        .withWrappingSequence(null)
        .withIntendedVehicle(vehicle.getReference());

    when(processabilityChecker.availableForTransportOrder(vehicle, order))
        .thenReturn(false);

    VehicleOrderSelection selection = strategy.selectVehicle(order);
    assertNotNull(selection);
    assertFalse(selection.isAssignable());
  }

  @Test
  public void returnUnassignableSelectionForUnavailableIntendedVehicleForSequence() {
    OrderSequence sequence = new OrderSequence("Some sequence")
        .withIntendedVehicle(vehicle.getReference())
        .withProcessingVehicle(null);

    TransportOrder order = createPlainTransportOrder()
        .withWrappingSequence(sequence.getReference())
        .withIntendedVehicle(vehicle.getReference());

    when(kernel.getTCSObject(OrderSequence.class, sequence.getReference()))
        .thenReturn(sequence);
    when(processabilityChecker.availableForTransportOrder(vehicle, order))
        .thenReturn(false);

    VehicleOrderSelection selection = strategy.selectVehicle(order);
    assertNotNull(selection);
    assertFalse(selection.isAssignable());
  }

  @Test
  public void returnUnassignableSelectionForUnavailableProcessingVehicleForSequence() {
    OrderSequence sequence = new OrderSequence("Some sequence")
        .withIntendedVehicle(vehicle.getReference())
        .withProcessingVehicle(vehicle.getReference());

    TransportOrder order = createPlainTransportOrder()
        .withWrappingSequence(sequence.getReference())
        .withIntendedVehicle(vehicle.getReference());

    when(kernel.getTCSObject(OrderSequence.class, sequence.getReference()))
        .thenReturn(sequence);
    when(processabilityChecker.availableForTransportOrder(vehicle, order))
        .thenReturn(false);

    VehicleOrderSelection selection = strategy.selectVehicle(order);
    assertNotNull(selection);
    assertFalse(selection.isAssignable());
  }

  @Test
  public void returnAssignableSelectionForAvailableIntendedVehicleForOrder() {
    TransportOrder order = createPlainTransportOrder()
        .withWrappingSequence(null)
        .withIntendedVehicle(vehicle.getReference());

    when(processabilityChecker.availableForTransportOrder(vehicle, order))
        .thenReturn(true);
    when(router.getRoute(vehicle, vehiclePosition, order))
        .thenReturn(Optional.of(new LinkedList<>()));

    VehicleOrderSelection selection = strategy.selectVehicle(order);
    assertNotNull(selection);
    assertTrue(selection.isAssignable());
  }

  @Test
  public void returnAssignableSelectionForAvailableIntendedVehicleForSequence() {
    OrderSequence sequence = new OrderSequence("Some sequence")
        .withIntendedVehicle(vehicle.getReference())
        .withProcessingVehicle(null);

    TransportOrder order = createPlainTransportOrder()
        .withWrappingSequence(sequence.getReference())
        .withIntendedVehicle(vehicle.getReference());

    when(kernel.getTCSObject(OrderSequence.class, sequence.getReference()))
        .thenReturn(sequence);
    when(processabilityChecker.availableForTransportOrder(vehicle, order))
        .thenReturn(true);
    when(router.getRoute(vehicle, vehiclePosition, order))
        .thenReturn(Optional.of(new LinkedList<>()));

    VehicleOrderSelection selection = strategy.selectVehicle(order);
    assertNotNull(selection);
    assertTrue(selection.isAssignable());
  }

  @Test
  public void returnAssignableSelectionForAvailableProcessingVehicleForSequence() {
    OrderSequence sequence = new OrderSequence("Some sequence")
        .withIntendedVehicle(vehicle.getReference())
        .withProcessingVehicle(vehicle.getReference());

    TransportOrder order = createPlainTransportOrder()
        .withWrappingSequence(sequence.getReference())
        .withIntendedVehicle(vehicle.getReference());

    when(kernel.getTCSObject(OrderSequence.class, sequence.getReference()))
        .thenReturn(sequence);
    when(processabilityChecker.availableForTransportOrder(vehicle, order))
        .thenReturn(true);
    when(router.getRoute(vehicle, vehiclePosition, order))
        .thenReturn(Optional.of(new LinkedList<>()));

    VehicleOrderSelection selection = strategy.selectVehicle(order);
    assertNotNull(selection);
    assertTrue(selection.isAssignable());
  }

  private TransportOrder createPlainTransportOrder() {
    Location destLocation
        = new Location("Some location", new LocationType("Some location type").getReference());
    List<DriveOrder> driveOrders
        = Arrays.asList(new DriveOrder(new Destination(destLocation.getReference())));
    return new TransportOrder("Some transport order", driveOrders);
  }
}
