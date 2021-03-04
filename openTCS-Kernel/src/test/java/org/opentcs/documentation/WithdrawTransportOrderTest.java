/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.documentation;

import java.util.Collections;
import org.junit.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;

/**
 * Test for the developers asciidoc documentation to show how a tranport order is withdrawn.
 * This test has no meaning and it just exists for the documentation to refer to and to guarantee
 * an example that compiles.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class WithdrawTransportOrderTest {

  private DispatcherService dispatcherService;
  private TCSObjectService objectService;
  private Vehicle vehicle;

  @Before
  public void setUp() {
    dispatcherService = mock(DispatcherService.class);
    objectService = mock(TCSObjectService.class);
    vehicle = new Vehicle("Vehicle");
    when(objectService.fetchObject(eq(Vehicle.class), any(String.class))).thenReturn(vehicle);
  }

  @Test
  public void shouldWithdrawTransportOrder() {
    // tag::documentation_withdrawTransportOrder[]
    // The dispatcher service instance we're working with
    DispatcherService dispatcherService = getDispatcherServiceFromSomewhere();

    // Get the transport order to be withdrawn.
    TransportOrder curOrder = getTransportOrderToWithdraw();
    // Withdraw the order.
    // The second argument indicates if the vehicle should finish the movements
    // it is already assigned to (false) or abort immediately (true).
    dispatcherService.withdrawByTransportOrder(curOrder.getReference(), true);
    // end::documentation_withdrawTransportOrder[]
  }

  @Test
  public void shouldWithdrawTransportOrderByVehicle() {
    // tag::documentation_withdrawTransportOrderByVehicle[]
    // The object service instance we're working with
    TCSObjectService objectService = getTCSObjectServiceFromSomewhere();

    // Get the vehicle from which the transport order shall be withdrawn
    Vehicle curVehicle = objectService.fetchObject(Vehicle.class,
                                                   getSampleVehicle());

    // The dispatcher service instance we're working with
    DispatcherService dispatcherService = getDispatcherServiceFromSomewhere();

    // Withdraw the order.
    // The second argument indicates if the vehicle should finish the movements
    // it is already assigned to (false) or abort immediately (true).
    dispatcherService.withdrawByVehicle(curVehicle.getReference(), true);
    // end::documentation_withdrawTransportOrderByVehicle[]
  }

  private Location getSampleDestinationLocation() {
    return new Location("Location", new LocationType("LocationType").getReference());
  }

  private String getSampleVehicle() {
    return "";
  }

  private DispatcherService getDispatcherServiceFromSomewhere() {
    return dispatcherService;
  }

  private TCSObjectService getTCSObjectServiceFromSomewhere() {
    return objectService;
  }

  private TransportOrder getTransportOrderToWithdraw() {
    return new TransportOrder(
        "Transportorder",
        Collections.singletonList(new DriveOrder(
            new DriveOrder.Destination(getSampleDestinationLocation().getReference())
                .withOperation(getDestinationOperation()))));
  }

  private String getDestinationOperation() {
    return "";
  }
}
