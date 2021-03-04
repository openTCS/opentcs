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
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.components.kernel.services.TransportOrderService;
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

  private TransportOrderService internalTransportOrderService;
  private DispatcherService dispatcherService;

  @Before
  public void setUp() {
    internalTransportOrderService = mock(InternalTransportOrderService.class);
    dispatcherService = mock(DispatcherService.class);
    when(internalTransportOrderService.createOrderSequence(any(OrderSequenceCreationTO.class)))
        .thenReturn(new OrderSequence("OrderSequence"));
    when(internalTransportOrderService.createTransportOrder(any(TransportOrderCreationTO.class)))
        .thenReturn(new TransportOrder(
            "Transportorder",
            Collections.singletonList(new DriveOrder(
                new DriveOrder.Destination(getSampleDestinationLocation().getReference())
                    .withOperation("some operation")))));
  }

  @Test
  public void shouldCreateTransportOrderSequence() {
    // tag::documentation_createTransportOrderSequence[]
    // The transport order service instance we're working with
    TransportOrderService transportOrderService = getATransportOrderServiceReference();

    // The dispatcher service instance we're working with
    DispatcherService dispatcherService = getADispatcherServiceReference();

    // Create an order sequence description with a unique name:
    OrderSequenceCreationTO sequenceTO
        = new OrderSequenceCreationTO("MyOrderSequence-" + UUID.randomUUID());
    // Optionally, set the sequence's failure-fatal flag:
    sequenceTO = sequenceTO.withFailureFatal(true);

    // Create the order sequence:
    OrderSequence orderSequence = transportOrderService.createOrderSequence(sequenceTO);

    // Set up the transport order as usual,
    // but add the wrapping sequence's name:
    List<DestinationCreationTO> destinations = new ArrayList<>();
    destinations.add(new DestinationCreationTO("Some location name",
                                               "Some operation"));
    TransportOrderCreationTO orderTO
        = new TransportOrderCreationTO("MyOrder-" + UUID.randomUUID(),
                                       destinations)
            .withWrappingSequence(orderSequence.getName());

    // Create the transport order:
    TransportOrder order = transportOrderService.createTransportOrder(orderTO);

    // Create and add more orders as necessary.
    // Eventually, set the order sequence's complete flag to indicate that more
    // transport orders will not be added to it.
    transportOrderService.markOrderSequenceComplete(orderSequence.getReference());

    // Trigger the dispatch process for the created order sequence.
    dispatcherService.dispatch();
    // end::documentation_createTransportOrderSequence[]
  }

  private Location getSampleDestinationLocation() {
    return new Location("Location",
                        new LocationType("LocationType").getReference());
  }

  private TransportOrderService getATransportOrderServiceReference() {
    return internalTransportOrderService;
  }

  private DispatcherService getADispatcherServiceReference() {
    return dispatcherService;
  }
}
