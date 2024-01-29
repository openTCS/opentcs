/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.documentation.developers_guide;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.TransportOrder;

/**
 * Test for the developers asciidoc documentation to show how a tranport order is created.
 * This test has no meaning and it just exists for the documentation to refer to and to guarantee
 * an example that compiles.
 */
class CreateTransportOrderTest {

  private TransportOrderService orderService;
  private DispatcherService dispService;

  @BeforeEach
  void setUp() {
    orderService = mock(InternalTransportOrderService.class);
    dispService = mock(DispatcherService.class);
    when(orderService.createTransportOrder(any(TransportOrderCreationTO.class)))
        .thenReturn(new TransportOrder(
            "Transportorder",
            Collections.singletonList(new DriveOrder(
                new Destination(someDestinationLocation().getReference())
                    .withOperation(getDestinationOperation())))));
  }

  @Test
  void demoCreateTransportOrderToLocation() {
    // Note: Keep these lines to a maximum of 80 characters for the documentation!

    // tag::createTransportOrder_createDestinations[]
    List<DestinationCreationTO> destinations
        = List.of(
            new DestinationCreationTO("Some location", "Some operation")
        );
    // end::createTransportOrder_createDestinations[]

    // tag::createTransportOrder_createTransportOrderCreationTO[]
    TransportOrderCreationTO orderTO
        = new TransportOrderCreationTO("MyTransportOrder", destinations);
    // end::createTransportOrder_createTransportOrderCreationTO[]

    // tag::createTransportOrder_setIncompleteName[]
    orderTO = orderTO.withIncompleteName(true);
    // end::createTransportOrder_setIncompleteName[]

    // tag::createTransportOrder_setMoreOptionalParameters[]
    orderTO = orderTO
        .withIntendedVehicleName("Some vehicle")
        .withDeadline(Instant.now().plus(1, ChronoUnit.HOURS));
    // end::createTransportOrder_setMoreOptionalParameters[]

    // tag::createTransportOrder_useServiceToCreateOrder[]
    TransportOrderService transportOrderService = getATransportOrderService();
    transportOrderService.createTransportOrder(orderTO);
    // end::createTransportOrder_useServiceToCreateOrder[]

    // tag::createTransportOrder_triggerDispatcher[]
    DispatcherService dispatcherService = getADispatcherService();
    dispatcherService.dispatch();
    // end::createTransportOrder_triggerDispatcher[]
  }

  @Test
  void demoCreateTransportOrderToPoint() {
    // Note: Keep these lines to a maximum of 80 characters for the documentation!

    // tag::createTransportOrderToPoint_createDestinations[]
    List<DestinationCreationTO> destinations
        = List.of(
            new DestinationCreationTO("Some point", Destination.OP_MOVE)
        );
    // end::createTransportOrderToPoint_createDestinations[]

    // tag::createTransportOrderToPoint_createTransportOrderCreationTO[]
    TransportOrderCreationTO orderTO
        = new TransportOrderCreationTO("MyTransportOrder", destinations)
            .withIntendedVehicleName("Some vehicle")
            .withIncompleteName(true);
    // end::createTransportOrderToPoint_createTransportOrderCreationTO[]

    // tag::createTransportOrderToPoint_useServiceToCreateOrder[]
    TransportOrderService transportOrderService = getATransportOrderService();
    transportOrderService.createTransportOrder(orderTO);
    // end::createTransportOrderToPoint_useServiceToCreateOrder[]

    // tag::createTransportOrderToPoint_triggerDispatcher[]
    DispatcherService dispatcherService = getADispatcherService();
    dispatcherService.dispatch();
    // end::createTransportOrderToPoint_triggerDispatcher[]
  }

  private Location someDestinationLocation() {
    return new Location("Location", new LocationType("LocationType").getReference());
  }

  private TransportOrderService getATransportOrderService() {
    return orderService;
  }

  private DispatcherService getADispatcherService() {
    return dispService;
  }

  private String getDestinationOperation() {
    return "";
  }
}
