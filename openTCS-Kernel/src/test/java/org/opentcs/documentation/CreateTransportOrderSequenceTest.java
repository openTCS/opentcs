/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.documentation;

import java.util.Collections;
import org.junit.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;

/**
 * Test for the developers asciidoc documentation to show how a tranport order sequence is created.
 * This test has no meaning and it just exists for the documentation to refer to and to guarantee
 * an example that compiles.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class CreateTransportOrderSequenceTest {

  private LocalKernel localKernel;

  private Vehicle vehicle;

  @Before
  public void setUp() {
    localKernel = mock(LocalKernel.class);
    vehicle = new Vehicle(3, "Vehicle");
    when(localKernel.createOrderSequence()).thenReturn(new OrderSequence(9, "OrderSequence"));
  }

  @Test
  public void shouldCreateTransportOrderSequence() {
    // tag::documentation_createTransportOrderSequence[]
    // The Kernel instance we're working with
    Kernel kernel = getKernelFromSomewhere();

    // Create an order sequence.
    OrderSequence orderSequence = kernel.createOrderSequence();

    // Set the order sequence's failureFatal flag (optional).
    kernel.setOrderSequenceFailureFatal(orderSequence.getReference(), true);

    // Create an order and set it up as usual, but do not activate it, yet!
    TransportOrder order = createARandomAndLongTransportOrder();

    // Add the order to the sequence.
    kernel.addOrderSequenceOrder(orderSequence.getReference(),
                                 order.getReference());

    // Activate the order when it may be processed by a vehicle.
    kernel.activateTransportOrder(order.getReference());

    // Create, add and activate more orders as necessary. As long as the sequence
    // has not been marked as complete and finished completely, the vehicle
    // selected for its first order will be tied to this sequence and will not
    // process any orders not belonging to the same sequence.
    // Eventually, set the order sequence's complete flag to indicate that more
    // transport orders will not be added to it.
    kernel.setOrderSequenceComplete(orderSequence.getReference());

    // Once the complete flag of the sequence has been set and all transport
    // orders belonging to it have been processed, its finished flag will be set
    // by the kernel.
    // end::documentation_createTransportOrderSequence[]
  }

  private TransportOrder createARandomAndLongTransportOrder() {
    return new TransportOrder(
        2,
        "Transportorder",
        Collections.singletonList(
            new DriveOrder.Destination(getSampleDestinationLocation().getReference(),
                                       getDestinationOperation())),
        0);
  }

  private String getDestinationOperation() {
    return "";
  }

  private Location getSampleDestinationLocation() {
    return new Location(0, "Location", new LocationType(1, "LocationType").getReference());
  }

  private LocalKernel getKernelFromSomewhere() {
    return localKernel;
  }
}
