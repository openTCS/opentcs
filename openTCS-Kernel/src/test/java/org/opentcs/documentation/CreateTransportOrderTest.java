/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.documentation;

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
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CreateTransportOrderTest {

  private TransportOrderService orderService;
  private DispatcherService dispService;

  @BeforeEach
  public void setUp() {
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
  public void demoCreateTransportOrderToLocation() {
    // Note: Keep these lines to a maximum of 80 characters for the documentation!
    // tag::documentation_createNewTransportOrder[]
    // Create a list of destinations the vehicle is supposed to travel to. Every
    // destination is described by the name of the destination location in the
    // plant model and an operation the vehicle is supposed to perform there.
    List<DestinationCreationTO> destinations
        = List.of(
            new DestinationCreationTO("Some location", "Some operation")
        );
    // Put as many destinations into the list as necessary. Then create a
    // transport order description with a name for the new transport order and
    // the list of destinations.
    // Note that the given name needs to be unique.
    TransportOrderCreationTO orderTO
        = new TransportOrderCreationTO("MyTransportOrder", destinations);
    // Optionally, express that the full name of the order should be generated
    // by the kernel. (If you do not do this, you need to ensure that the name
    // of the transport order given above is unique.)
    orderTO = orderTO.withIncompleteName(true);
    // Optionally, assign a specific vehicle to the transport order.
    orderTO = orderTO.withIntendedVehicleName("Some vehicle");
    // Optionally, set a deadline for the transport order.
    orderTO = orderTO.withDeadline(Instant.now().plus(1, ChronoUnit.HOURS));

    // Get a TransportOrderService and ask it to create a transport order using
    // the given description.
    TransportOrderService transportOrderService
        = getATransportOrderServiceReference();
    transportOrderService.createTransportOrder(orderTO);

    // Optionally, get a DispatcherService and trigger the kernel's dispatcher
    // explicitly to have it check for a vehicle that can process the transport
    // order. (You only need to do this if you need the dispatcher to be
    // triggered immediately after creating the transport order. If you do not
    // do this, the dispatcher will still be triggered periodically.)
    DispatcherService dispatcherService = getADispatcherServiceReference();
    dispatcherService.dispatch();
    // end::documentation_createNewTransportOrder[]
  }

  @Test
  public void demoCreateTransportOrderToPoint() {
    // Note: Keep these lines to a maximum of 80 characters for the documentation!
    // tag::documentation_createNewTransportOrderToPoint[]
    // Create a list containing a single destination to a point, using
    // Destination.OP_MOVE as the operation to be executed.
    List<DestinationCreationTO> destinations
        = List.of(
            new DestinationCreationTO("Some point", Destination.OP_MOVE)
        );

    // Create a transport order description with a name for the new transport
    // order and the (single-element) list of destinations.
    TransportOrderCreationTO orderTO
        = new TransportOrderCreationTO("MyTransportOrder", destinations)
            .withIntendedVehicleName("Some vehicle")
            .withIncompleteName(true);

    // Get a TransportOrderService and ask it to create a transport order using
    // the given description.
    TransportOrderService transportOrderService
        = getATransportOrderServiceReference();
    transportOrderService.createTransportOrder(orderTO);

    // Optionally, get a DispatcherService and trigger the kernel's dispatcher
    // explicitly to have it check for a vehicle that can process the transport
    // order. (You only need to do this if you need the dispatcher to be
    // triggered immediately after creating the transport order. If you do not
    // do this, the dispatcher will still be triggered periodically.)
    DispatcherService dispatcherService = getADispatcherServiceReference();
    dispatcherService.dispatch();
    // end::documentation_createNewTransportOrderToPoint[]
  }

  private Location someDestinationLocation() {
    return new Location("Location", new LocationType("LocationType").getReference());
  }

  private TransportOrderService getATransportOrderServiceReference() {
    return orderService;
  }

  private DispatcherService getADispatcherServiceReference() {
    return dispService;
  }

  private String getDestinationOperation() {
    return "";
  }
}
