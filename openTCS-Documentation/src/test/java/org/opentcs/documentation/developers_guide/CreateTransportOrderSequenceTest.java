/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.documentation.developers_guide;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
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
 */
class CreateTransportOrderSequenceTest {

  private TransportOrderService orderService;

  @BeforeEach
  void setUp() {
    orderService = mock(InternalTransportOrderService.class);
    when(orderService.createOrderSequence(any(OrderSequenceCreationTO.class)))
        .thenReturn(new OrderSequence("OrderSequence"));
    when(orderService.createTransportOrder(any(TransportOrderCreationTO.class)))
        .thenReturn(new TransportOrder(
            "Transportorder",
            Collections.singletonList(new DriveOrder(
                new DriveOrder.Destination(getSampleDestinationLocation().getReference())
                    .withOperation("some operation")))));
  }

  @Test
  void demoCreateOrderSequence() {
    // Note: Keep these lines to a maximum of 80 characters for the documentation!

    // tag::createOrderSequence_createOrderSequenceCreationTO[]
    OrderSequenceCreationTO sequenceTO
        = new OrderSequenceCreationTO("MyOrderSequence");
    // end::createOrderSequence_createOrderSequenceCreationTO[]

    // tag::createOrderSequence_setIncompleteName[]
    sequenceTO = sequenceTO.withIncompleteName(true);
    // end::createOrderSequence_setIncompleteName[]

    // tag::createOrderSequence_setFailureFatal[]
    sequenceTO = sequenceTO.withFailureFatal(true);
    // end::createOrderSequence_setFailureFatal[]

    // tag::createOrderSequence_useServiceToCreateSequence[]
    TransportOrderService transportOrderService = getATransportOrderService();
    OrderSequence orderSequence
        = transportOrderService.createOrderSequence(sequenceTO);
    // end::createOrderSequence_useServiceToCreateSequence[]

    // tag::createOrderSequence_createTransportOrder[]
    TransportOrderCreationTO orderTO
        = new TransportOrderCreationTO(
            "MyOrder",
            List.of(
                new DestinationCreationTO("Some location", "Some operation")
            )
        )
            .withIncompleteName(true)
            .withWrappingSequence(orderSequence.getName());

    transportOrderService.createTransportOrder(orderTO);
    // end::createOrderSequence_createTransportOrder[]

    // tag::createOrderSequence_markSequenceComplete[]
    transportOrderService.markOrderSequenceComplete(
        orderSequence.getReference()
    );
    // end::createOrderSequence_markSequenceComplete[]
  }

  private Location getSampleDestinationLocation() {
    return new Location("Location",
                        new LocationType("LocationType").getReference());
  }

  private TransportOrderService getATransportOrderService() {
    return orderService;
  }
}
