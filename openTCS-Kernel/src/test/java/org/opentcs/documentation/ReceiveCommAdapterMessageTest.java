/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.documentation;

import java.util.ArrayList;
import java.util.List;
import org.junit.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.TransportOrder;

/**
 * Test for the developers asciidoc documentation to show how to receive messages from communication
 * adapters.
 * This test has no meaning and it just exists for the documentation to refer to and to guarantee
 * an example that compiles.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ReceiveCommAdapterMessageTest {

  private LocalKernel localKernel;

  private Vehicle sampleVehicle;

  private TransportOrder sampleTransportOrder;

  @Before
  public void setUp() {
    localKernel = mock(LocalKernel.class);
    sampleVehicle = createSampleVehicle();
    sampleTransportOrder = createSampleTransportOrder();
    when(localKernel.getTCSObject(eq(Vehicle.class), any(String.class))).thenReturn(sampleVehicle);
    when(localKernel.getTCSObject(eq(TransportOrder.class), any(String.class)))
        .thenReturn(sampleTransportOrder);
  }

  @Test
  public void shouldReceiveMessageFromVehicle() {
    // tag::documentation_receiveMessageFromVehicle[]
    // The Kernel instance we're working with
    Kernel kernel = getKernelFromSomewhere();

    // Get the vehicle from which information shall be retrieved
    Vehicle vehicle = kernel.getTCSObject(Vehicle.class, getTheVehicleName());

    // Get the actual property you're looking for
    String property = vehicle.getProperty("someKey");
    // end::documentation_receiveMessageFromVehicle[]
    Assert.assertEquals("someValue", property);
  }

  @Test
  public void shouldReceiveMessageFromTransportOrder() {
    // tag::documentation_receiveMessageFromTransportOrder[]
    // The Kernel instance we're working with
    Kernel kernel = getKernelFromSomewhere();

    // Get the tansport order from which information shall be retrieved
    TransportOrder transportOrder = kernel.getTCSObject(TransportOrder.class,
                                                        getTheTransportOrderName());

    // Get the actual property you're looking for
    String property = transportOrder.getProperty("someKey");
    // end::documentation_receiveMessageFromTransportOrder[]
    Assert.assertEquals("someValue", property);
  }

  private TransportOrder createSampleTransportOrder() {
    List<Destination> destinations = new ArrayList<>();
    Destination dest = new Destination(getSampleDestinationLocation().getReference(), "");
    destinations.add(dest);
    TransportOrder someTransportOrder = new TransportOrder(3, "TransportOrder-01", destinations, 0);
    someTransportOrder.setProperty("someKey", "someValue");
    return someTransportOrder;
  }

  private Location getSampleDestinationLocation() {
    return new Location(0, "Location-01", new LocationType(1, "LocationType-01").getReference());
  }

  private Vehicle createSampleVehicle() {
    Vehicle someVehicle = new Vehicle(1, "Vehicle-01");
    someVehicle.setProperty("someKey", "someValue");
    return someVehicle;
  }

  private String getTheVehicleName() {
    return sampleVehicle.getName();
  }

  private String getTheTransportOrderName() {
    return sampleTransportOrder.getName();
  }

  private LocalKernel getKernelFromSomewhere() {
    return localKernel;
  }
}
