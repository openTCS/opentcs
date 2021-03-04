/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.xmlorders.binding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.assertTrue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class TCSOrderSetTest {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TCSOrderSetTest.class);

  public TCSOrderSetTest() {
  }

  @Test
  public void shouldOutputSampleOrder() {
    TCSOrderSet orderSet = new TCSOrderSet();

    Transport transport = new Transport();
    transport.setId("TransportOrder-01");
    transport.setDeadline(new Date());
    transport.setIntendedVehicle("Vehicle-01");

    Destination dest = new Destination();
    dest.setLocationName("Storage 01");
    dest.setOperation("Load cargo");
    transport.getDestinations().add(dest);

    dest = new Destination();
    dest.setLocationName("Storage 02");
    dest.setOperation("Unoad cargo");
    transport.getDestinations().add(dest);

    transport.getProperties().add(new Property("waitBefore", "Unload"));

    orderSet.getOrders().add(transport);

    transport = new Transport();
    transport.setId("TransportOrder-02");

    dest = new Destination();
    dest.setLocationName("Working station 01");
    dest.setOperation("Drill");
    dest.getProperties().add(new Property("drillSize", "3"));
    transport.getDestinations().add(dest);

    dest = new Destination();
    dest.setLocationName("Working station 02");
    dest.setOperation("Drill");
    dest.getProperties().add(new Property("drillSize", "8"));
    transport.getDestinations().add(dest);

    dest = new Destination();
    dest.setLocationName("Working station 03");
    dest.setOperation("Cut");
    transport.getDestinations().add(dest);

    orderSet.getOrders().add(transport);

    String xmlOutput = orderSet.toXml();
    LOG.info(xmlOutput);

    TCSOrderSet parsedOrderSet = TCSOrderSet.fromXml(xmlOutput);
    assertTrue(parsedOrderSet.getOrders().size() == 2);
  }
  
  @Test
  public void shouldOutputSampleScriptOrder() {
    TCSOrderSet orderSet = new TCSOrderSet();
    
    TransportScript transportScript = new TransportScript();
    transportScript.setId("test.tcs");
    transportScript.setFileName("test.tcs");
    
    orderSet.getOrders().add(transportScript);
    
    String xmlOutput = orderSet.toXml();
    LOG.info(xmlOutput);

    TCSOrderSet parsedOrderSet = TCSOrderSet.fromXml(xmlOutput);
    assertTrue(parsedOrderSet.getOrders().size() == 1);
  }

  @Test
  public void shouldMarshallAndUnmarshall() {
    List<TCSOrder> orders = Arrays.asList(createTransportOrder(1),
                                          createTransportOrder(2));
    TCSOrderSet orderSetFromOrders = new TCSOrderSet();
    orderSetFromOrders.setOrders(orders);

    // Test toXml
    String result = orderSetFromOrders.toXml();
    LOG.info(result);

    // Test fromXml
    TCSOrderSet orderSetFromXml = TCSOrderSet.fromXml(result);
    Transport order = (Transport) orderSetFromXml.getOrders().get(0);
    assertTrue(order.getId().equals("TransportOrder-01"));
    Destination destination = order.getDestinations().get(1);
    assertTrue(destination.getLocationName().equals("Location 2"));
    Property property = destination.getProperties().get(0);
    assertTrue(property.getValue().equals("Value 1"));
  }

  private Transport createTransportOrder(int orderNumber) {
    Transport order = new Transport();
    order.setId("TransportOrder-0" + orderNumber);
    order.setDeadline(GregorianCalendar.getInstance().getTime());
    order.setIntendedVehicle("Vehicle-0" + orderNumber);
    List<Destination> destinations = new ArrayList<>();
    destinations.add(createDestination("Location " + ((orderNumber * 2) - 1), "Load"));
    destinations.add(createDestination("Location " + (orderNumber * 2), "Unload"));
    order.setDestinations(destinations);
    return order;
  }

  private Destination createDestination(String location, String operation) {
    Destination destination = new Destination();
    destination.setLocationName(location);
    destination.setOperation(operation);
    List<Property> properties = new LinkedList<>();
    Property property = new Property();
    property.setKey("Key 1");
    property.setValue("Value 1");
    properties.add(property);
    property = new Property();
    property.setKey("Key 2");
    property.setValue("Value 2");
    properties.add(property);
    destination.setProperties(properties);
    return destination;
  }
}
