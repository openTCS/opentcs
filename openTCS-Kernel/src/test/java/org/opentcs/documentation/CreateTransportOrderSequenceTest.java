/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.documentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
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

  @Before
  public void setUp() {
    localKernel = mock(LocalKernel.class);
    when(localKernel.createOrderSequence(any(OrderSequenceCreationTO.class)))
        .thenReturn(new OrderSequence("OrderSequence"));
    when(localKernel.createTransportOrder(any(TransportOrderCreationTO.class)))
        .thenReturn(new TransportOrder(
            "Transportorder",
            Collections.singletonList(new DriveOrder(
                new DriveOrder.Destination(getSampleDestinationLocation().getReference())
                    .withOperation("some operation")))));
  }

  @Test
  public void shouldCreateTransportOrderSequence() {
    // tag::documentation_createTransportOrderSequence[]
    // The Kernel instance we're working with
    Kernel kernel = getAKernelReference();

    // Create an order sequence description with a unique name:
    OrderSequenceCreationTO sequenceTO
        = new OrderSequenceCreationTO("MyOrderSequence-" + UUID.randomUUID());
    // Optionally, set the sequence's failure-fatal flag:
    sequenceTO.setFailureFatal(true);

    // Create the order sequence:
    OrderSequence orderSequence = kernel.createOrderSequence(sequenceTO);

    // Set up the transport order as usual:
    List<DestinationCreationTO> destinations = new ArrayList<>();
    destinations.add(new DestinationCreationTO("Some location name",
                                               "Some operation"));
    TransportOrderCreationTO orderTO
        = new TransportOrderCreationTO("MyOrder-" + UUID.randomUUID(),
                                       destinations);
    // Set the name of the wrapping order sequence:
    orderTO.setWrappingSequence(orderSequence.getName());

    // Create the transport order and activate it for processing:
    TransportOrder order = kernel.createTransportOrder(orderTO);
    // Activate the order when it may be processed by a vehicle.
    kernel.activateTransportOrder(order.getReference());

    // Create, add and activate more orders as necessary.
    // Eventually, set the order sequence's complete flag to indicate that more
    // transport orders will not be added to it.
    kernel.setOrderSequenceComplete(orderSequence.getReference());
    // end::documentation_createTransportOrderSequence[]
  }

  private Location getSampleDestinationLocation() {
    return new Location("Location",
                        new LocationType("LocationType").getReference());
  }

  private LocalKernel getAKernelReference() {
    return localKernel;
  }
}
