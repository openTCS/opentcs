/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.documentation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
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

  private Vehicle vehicle;

  @Before
  public void setUp() {
    localKernel = mock(LocalKernel.class);
    vehicle = new Vehicle(3, "Vehicle");
    when(localKernel.createTransportOrder(any()))
        .thenReturn(new TransportOrder(
            2,
            "Transportorder",
            Collections.singletonList(new Destination(getSampleDestinationLocation().getReference(),
                                                      getDestinationOperation())),
            0));
  }

  @Test
  public void shouldCreateAndActivateTransportOrder() {
    // tag::documentation_createNewTransportOrder[]
    // The Kernel instance we're working with
    Kernel kernel = getKernelFromSomewhere();

    // A list of Destination instances the transport order shall consist of:
    List<Destination> destinations = new LinkedList<>();

    // Every single destination of the order has to be added to the list like this:
    // Get a reference to the location to move to...
    // (You can get a set of all existing Location instances using
    // kernel.getTCSObjects(Location.class).)
    Location destLoc = getSampleDestinationLocation();
    TCSObjectReference<Location> destLocRef = destLoc.getReference();

    // ...and an operation the vehicle should execute at the location.
    // (You can get a list of all operations allowed at the chosen location by
    // looking into the LocationType instance that destLoc.getType() references.)
    String destOp = getDestinationOperation();
    // Create a new Destination instance and add it to the list.
    destinations.add(new Destination(destLocRef, destOp));

    // Add as many destinations to the list like this as necessary.
    // Eventually create a new transport order with these destinations:
    TransportOrder newOrder = kernel.createTransportOrder(destinations);

    // Assign a vehicle to the transport order (optional)
    kernel.setTransportOrderIntendedVehicle(newOrder.getReference(),
                                            getSampleVehicle().getReference());

    // Assign a deadline to the transport order (optional)
    kernel.setTransportOrderDeadline(newOrder.getReference(),
                                     getSampleTimestamp());

    // And at last activate the transport order
    kernel.activateTransportOrder(newOrder.getReference());
    // Once a vehicle is available and able to process the transport order, the
    // kernel will assign it immediately.
    // end::documentation_createNewTransportOrder[]
  }

  @Test
  public void shouldCreateTransportOrderToAPoint() {
    // tag::documentation_createNewTransportOrderToPoint[]
    // The Kernel instance we're working with
    Kernel kernel = getKernelFromSomewhere();

    // The point the vehicle shall be sent to
    Point destPos = getSamplePoint();

    // Wrap the name of the point in a dummy location reference
    TCSObjectReference<Location> dummyLocRef
        = TCSObjectReference.getDummyReference(Location.class, destPos.getName());

    // Create a Destination instance using the dummy location reference and use
    // Destination.OP_MOVE as the operation to be executed.
    Destination dummyDest = new Destination(dummyLocRef, Destination.OP_MOVE);

    // Wrap the Destination instance in a list.
    List<Destination> dummyDests = Collections.singletonList(dummyDest);

    // Create a transport order using the list
    TransportOrder dummyOrder = kernel.createTransportOrder(dummyDests);
    // Assign a specific vehicle to the transport order (optional)
    kernel.setTransportOrderIntendedVehicle(dummyOrder.getReference(),
                                            vehicle.getReference());

    // Activate the new transport order
    kernel.activateTransportOrder(dummyOrder.getReference());

    // Once a vehicle is available and able to process the transport order, the
    // kernel will assign it immediately.
    // end::documentation_createNewTransportOrderToPoint[]
  }

  private Point getSamplePoint() {
    return new Point(5, "Point");
  }

  private Location getSampleDestinationLocation() {
    return new Location(0, "Location", new LocationType(1, "LocationType").getReference());
  }

  private Vehicle getSampleVehicle() {
    return vehicle;
  }

  private long getSampleTimestamp() {
    return 0;
  }

  private LocalKernel getKernelFromSomewhere() {
    return localKernel;
  }

  private String getDestinationOperation() {
    return "";
  }
}
