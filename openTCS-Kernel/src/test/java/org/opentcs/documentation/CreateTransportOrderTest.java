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
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.junit.*;
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
 */
public class CreateTransportOrderTest {

  private TransportOrderService internalTransportOrderService;
  private DispatcherService dispatcherService;

  @Before
  public void setUp() {
    internalTransportOrderService = mock(InternalTransportOrderService.class);
    dispatcherService = mock(DispatcherService.class);
    when(internalTransportOrderService.createTransportOrder(any(TransportOrderCreationTO.class)))
        .thenReturn(new TransportOrder(
            "Transportorder",
            Collections.singletonList(new DriveOrder(
                new Destination(someDestinationLocation().getReference())
                    .withOperation(getDestinationOperation())))));
  }

  @Test
  public void shouldCreateAndActivateTransportOrder() {
    // tag::documentation_createNewTransportOrder[]
    // The transport order service instance we're working with
    TransportOrderService transportOrderService = getATransportOrderServiceReference();

    // The dispatcher service instance we're working with
    DispatcherService dispatcherService = getADispatcherServiceReference();

    // A list of destinations the transport order the vehicle is supposed
    // to travel to:
    List<DestinationCreationTO> destinations = new LinkedList<>();
    // Create a new destination description and add it to the list.
    // Every destination is described by the name of the destination
    // location in the plant model and an operation the vehicle is supposed
    // to perform there:
    destinations.add(new DestinationCreationTO("Some location name",
                                               "Some operation"));
    // Add as many destinations to the list like this as necessary. Then
    // create a transport order description with a name for the new transport
    // order and the list of destinations.
    // Note that the given name needs to be unique.
    TransportOrderCreationTO orderTO
        = new TransportOrderCreationTO("MyTransportOrder-" + UUID.randomUUID(),
                                       destinations);
    // Optionally, assign a specific vehicle to the transport order:
    orderTO = orderTO.withIntendedVehicleName("Some vehicle name");
    // Optionally, set a deadline for the transport order:
    orderTO = orderTO.withDeadline(Instant.now().plus(1, ChronoUnit.HOURS));

    // Create a new transport order for the given description:
    TransportOrder newOrder = transportOrderService.createTransportOrder(orderTO);

    // Trigger the dispatch process for the created transport order.
    dispatcherService.dispatch();
    // end::documentation_createNewTransportOrder[]
  }

  @Test
  public void shouldCreateTransportOrderToAPoint() {
    // tag::documentation_createNewTransportOrderToPoint[]
    // The transport order service instance we're working with
    TransportOrderService transportOrderService = getATransportOrderServiceReference();

    // The dispatcher service instance we're working with
    DispatcherService dispatcherService = getADispatcherServiceReference();

    // Create a list containing a single destination to a point.
    // Use Destination.OP_MOVE as the operation to be executed:
    List<DestinationCreationTO> destinations = new LinkedList<>();
    destinations.add(new DestinationCreationTO("Some point name",
                                               Destination.OP_MOVE));
    // Create a transport order description with the destination and a
    // unique name and assign it to a specific vehicle:
    TransportOrderCreationTO orderTO
        = new TransportOrderCreationTO("MyTransportOrder-" + UUID.randomUUID(),
                                       destinations)
            .withIntendedVehicleName("Some vehicle name");

    // Create a transport order using the description:
    TransportOrder dummyOrder = transportOrderService.createTransportOrder(orderTO);

    // Trigger the dispatch process for the created transport order.
    dispatcherService.dispatch();
    // end::documentation_createNewTransportOrderToPoint[]
  }

  private Location someDestinationLocation() {
    return new Location("Location", new LocationType("LocationType").getReference());
  }

  private TransportOrderService getATransportOrderServiceReference() {
    return internalTransportOrderService;
  }

  private DispatcherService getADispatcherServiceReference() {
    return dispatcherService;
  }

  private String getDestinationOperation() {
    return "";
  }
}
