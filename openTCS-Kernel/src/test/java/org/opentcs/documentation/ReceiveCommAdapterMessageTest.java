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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
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

  private TCSObjectService objectService;

  private Vehicle sampleVehicle;

  private TransportOrder sampleTransportOrder;

  @Before
  public void setUp() {
    objectService = mock(TCSObjectService.class);
    sampleVehicle = createSampleVehicle();
    sampleTransportOrder = createSampleTransportOrder();
    when(objectService.fetchObject(eq(Vehicle.class), any(String.class))).thenReturn(sampleVehicle);
    when(objectService.fetchObject(eq(TransportOrder.class), any(String.class)))
        .thenReturn(sampleTransportOrder);
  }

  @Test
  public void shouldReceiveMessageFromVehicle() {
    // tag::documentation_receiveMessageFromVehicle[]
    // The object service instance we're working with
    TCSObjectService objectService = getTCSObjectServiceFromSomewhere();

    // Get the vehicle from which information shall be retrieved
    Vehicle vehicle = objectService.fetchObject(Vehicle.class, getTheVehicleName());

    // Get the actual property you're looking for
    String property = vehicle.getProperty("someKey");
    // end::documentation_receiveMessageFromVehicle[]
    Assert.assertEquals("someValue", property);
  }

  @Test
  public void shouldReceiveMessageFromTransportOrder() {
    // tag::documentation_receiveMessageFromTransportOrder[]
    // The Kernel instance we're working with
    TCSObjectService objectService = getTCSObjectServiceFromSomewhere();

    // Get the tansport order from which information shall be retrieved
    TransportOrder transportOrder = objectService.fetchObject(TransportOrder.class,
                                                              getTheTransportOrderName());

    // Get the actual property you're looking for
    String property = transportOrder.getProperty("someKey");
    // end::documentation_receiveMessageFromTransportOrder[]
    Assert.assertEquals("someValue", property);
  }

  private TransportOrder createSampleTransportOrder() {
    List<DriveOrder> driveOrders = new ArrayList<>();
    Destination dest = new Destination(getSampleDestinationLocation().getReference());
    driveOrders.add(new DriveOrder(dest));
    TransportOrder someTransportOrder = new TransportOrder("TransportOrder-01", driveOrders)
        .withProperty("someKey", "someValue");
    return someTransportOrder;
  }

  private Location getSampleDestinationLocation() {
    return new Location("Location-01", new LocationType("LocationType-01").getReference());
  }

  private Vehicle createSampleVehicle() {
    Vehicle someVehicle = new Vehicle("Vehicle-01")
        .withProperty("someKey", "someValue");
    return someVehicle;
  }

  private String getTheVehicleName() {
    return sampleVehicle.getName();
  }

  private String getTheTransportOrderName() {
    return sampleTransportOrder.getName();
  }

  private TCSObjectService getTCSObjectServiceFromSomewhere() {
    return objectService;
  }
}
