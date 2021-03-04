/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import java.util.ArrayList;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.vehicleselection.AssignedVehicleSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.vehicleselection.AvailableVehicleSelectionStrategy;

/**
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class VehicleSelectorTest {

  /**
   * The class to test.
   */
  private VehicleSelector vehicleSelector;

  private AssignedVehicleSelectionStrategy assignedVehicleSelectionStrategy;

  private AvailableVehicleSelectionStrategy availableVehicleSelectionStrategy;

  private CompositeTransportOrderSelectionVeto transportOrderSelectionVeto;

  private static final String TRANSPORT_ORDER_NAME = "SomeTransporOrderName";

  private static final String VEHICLE_NAME = "SomeVehicleName";

  public VehicleSelectorTest() {
  }

  @Before
  public void setUp() {
    assignedVehicleSelectionStrategy = mock(AssignedVehicleSelectionStrategy.class);
    availableVehicleSelectionStrategy = mock(AvailableVehicleSelectionStrategy.class);
    transportOrderSelectionVeto = mock(CompositeTransportOrderSelectionVeto.class);
    vehicleSelector = new VehicleSelector(assignedVehicleSelectionStrategy,
                                          availableVehicleSelectionStrategy,
                                          transportOrderSelectionVeto);
  }

  @Test
  public void shouldIgnoreTransportOrderWithVeto() {
    when(transportOrderSelectionVeto.test(any(TransportOrder.class)))
        .thenReturn(true);

    TransportOrder order = new TransportOrder(TRANSPORT_ORDER_NAME, new ArrayList<>());
    VehicleOrderSelection result = vehicleSelector.selectVehicle(order);

    assertNull(result.getVehicle());
  }

  @Test
  public void shouldReturnVehicleForTransportOrder() {
    when(assignedVehicleSelectionStrategy.selectVehicle(any(TransportOrder.class)))
        .thenReturn(createVehicleOrderSelection());

    TransportOrder order = new TransportOrder(TRANSPORT_ORDER_NAME, new ArrayList<>());
    VehicleOrderSelection result = vehicleSelector.selectVehicle(order);

    assertNotNull(result.getVehicle());
    assertEquals(VEHICLE_NAME, result.getVehicle().getName());
  }

  private VehicleOrderSelection createVehicleOrderSelection() {
    TransportOrder order = new TransportOrder(TRANSPORT_ORDER_NAME, new ArrayList<>());
    Vehicle vehicle = new Vehicle(VEHICLE_NAME);
    VehicleOrderSelection vehicleOrderSelection
        = new VehicleOrderSelection(order,
                                    vehicle,
                                    new ArrayList<>());
    return vehicleOrderSelection;
  }
}
