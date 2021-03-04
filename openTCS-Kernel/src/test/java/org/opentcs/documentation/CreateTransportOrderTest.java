/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.documentation;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.junit.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
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

  private LocalKernel localKernel;

  @Before
  public void setUp() {
    localKernel = mock(LocalKernel.class);
    when(localKernel.createTransportOrder(any(TransportOrderCreationTO.class)))
        .thenReturn(new TransportOrder(
            "Transportorder",
            Collections.singletonList(new DriveOrder(
                new Destination(someDestinationLocation().getReference())
                    .withOperation(getDestinationOperation())))));
  }

  @Test
  public void shouldCreateAndActivateTransportOrder() {
    // tag::documentation_createNewTransportOrder[]
    // The Kernel instance we're working with
    Kernel kernel = getAKernelReference();

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
    orderTO.setIntendedVehicleName("Some vehicle name");
    // Optionally, set a deadline for the transport order:
    orderTO.setDeadline(ZonedDateTime.of(2099, 12, 31, 23, 59, 59, 0, ZoneId.of("Europe/Berlin")));

    // Create a new transport order for the given description:
    TransportOrder newOrder = kernel.createTransportOrder(orderTO);
    // And at last activate the transport order
    kernel.activateTransportOrder(newOrder.getReference());
    // Once a vehicle is available and able to process the transport order,
    // the kernel will assign it.
    // end::documentation_createNewTransportOrder[]
  }

  @Test
  public void shouldCreateTransportOrderToAPoint() {
    // tag::documentation_createNewTransportOrderToPoint[]
    // The Kernel instance we're working with
    Kernel kernel = getAKernelReference();

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
            .setIntendedVehicleName("Some vehicle name");

    // Create a transport order using the description:
    TransportOrder dummyOrder = kernel.createTransportOrder(orderTO);
    // Activate the new transport order:
    kernel.activateTransportOrder(dummyOrder.getReference());
    // end::documentation_createNewTransportOrderToPoint[]
  }

  private Location someDestinationLocation() {
    return new Location("Location", new LocationType("LocationType").getReference());
  }

  private LocalKernel getAKernelReference() {
    return localKernel;
  }

  private String getDestinationOperation() {
    return "";
  }
}
