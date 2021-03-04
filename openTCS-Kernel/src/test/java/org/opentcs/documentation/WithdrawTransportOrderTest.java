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
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
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

  private LocalKernel localKernel;

  private Vehicle vehicle;

  @Before
  public void setUp() {
    localKernel = mock(LocalKernel.class);
    vehicle = new Vehicle("Vehicle");
    when(localKernel.getTCSObject(eq(Vehicle.class), any(String.class))).thenReturn(vehicle);
  }

  @Test
  public void shouldWithdrawTransportOrder() {
    // tag::documentation_withdrawTransportOrder[]
    // The Kernel instance we're working with
    Kernel kernel = getKernelFromSomewhere();

    // Get the transport order to be withdrawn.
    TransportOrder curOrder = getTransportOrderToWithdraw();
    // Withdraw the order.
    // The second argument indicates if the vehicle should finish the movements
    // it is already assigned to (false) or abort immediately (true).
    // The third argument indicates whether the vehicle's processing state should
    // be changed to UNAVAILABLE so it cannot be assigned another transport order
    // right after the withdrawal.
    kernel.withdrawTransportOrder(curOrder.getReference(), true, false);
    // end::documentation_withdrawTransportOrder[]
  }

  @Test
  public void shouldWithdrawTransportOrderByVehicle() {
    // tag::documentation_withdrawTransportOrderByVehicle[]
    // The Kernel instance we're working with
    Kernel kernel = getKernelFromSomewhere();

    // Get the vehicle from which the transport order shall be withdrawn
    Vehicle curVehicle = kernel.getTCSObject(Vehicle.class,
                                             getSampleVehicle());
    // Withdraw the order.
    // The second argument indicates if the vehicle should finish the movements
    // it is already assigned to (false) or abort immediately (true).
    // The third argument indicates whether the vehicle's processing state should
    // be changed to UNAVAILABLE so it cannot be assigned another transport order
    // right after the withdrawal.
    kernel.withdrawTransportOrderByVehicle(curVehicle.getReference(), true, false);
    // end::documentation_withdrawTransportOrderByVehicle[]
  }

  private Location getSampleDestinationLocation() {
    return new Location("Location", new LocationType("LocationType").getReference());
  }

  private String getSampleVehicle() {
    return "";
  }

  private LocalKernel getKernelFromSomewhere() {
    return localKernel;
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
