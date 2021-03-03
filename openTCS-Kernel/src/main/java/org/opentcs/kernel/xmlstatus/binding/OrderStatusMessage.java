/*
 * openTCS copyright information:
 * Copyright (c) 2008 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.xmlstatus.binding;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.order.TransportOrder.State;

/**
 * A status message containing details about a transport order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OrderStatusMessage
    extends StatusMessage {

  /**
   * The transport order's name.
   */
  private String orderName = "";
  /**
   * The transport order's state.
   */
  private TransportOrder.State orderState;
  /**
   * The list of destinations making up the transport order.
   */
  private List<Destination> destinations = new LinkedList<>();
  /**
   * The properties of the transport order.
   */
  private List<Property> properties = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public OrderStatusMessage() {
  }

  /**
   * Returns the transport order's name.
   *
   * @return The transport order's name.
   */
  @XmlAttribute(name = "orderName", required = true)
  public String getOrderName() {
    return orderName;
  }

  /**
   * Sets the transport order's name.
   *
   * @param orderName The transport order's name.
   */
  public void setOrderName(String orderName) {
    this.orderName = orderName;
  }

  /**
   * Returns the transport order's current state.
   *
   * @return The transport order's current state.
   */
  @XmlAttribute(name = "orderState", required = true)
  public State getOrderState() {
    return orderState;
  }

  /**
   * Sets the transport order's current state.
   *
   * @param orderState The transport order's current state.
   */
  public void setOrderState(State orderState) {
    this.orderState = orderState;
  }

  /**
   * Returns the transport order's destinations.
   *
   * @return The transport order's destinations.
   */
  @XmlElement(name = "destination", required = false)
  public List<Destination> getDestinations() {
    return destinations;
  }

  /**
   * Sets the transport order's destinations.
   *
   * @param destinations The transport order's destinations.
   */
  public void setDestinations(List<Destination> destinations) {
    this.destinations = destinations;
  }

  @XmlElement(name = "property", required = false)
  public List<Property> getProperties() {
    return properties;
  }

  public void setProperties(List<Property> properties) {
    this.properties = properties;
  }

  public static OrderStatusMessage fromTransportOrder(TransportOrder order) {
    OrderStatusMessage orderMessage = new OrderStatusMessage();
    orderMessage.setOrderName(order.getName());
    orderMessage.setOrderState(order.getState());
    for (DriveOrder curDriveOrder : order.getAllDriveOrders()) {
      Destination dest = new Destination();
      dest.setLocationName(curDriveOrder.getDestination().getLocation().getName());
      dest.setOperation(curDriveOrder.getDestination().getOperation());
      dest.setState(curDriveOrder.getState());
      for (Map.Entry<String, String> mapEntry
               : curDriveOrder.getDestination().getProperties().entrySet()) {
        Property prop = new Property();
        prop.setKey(mapEntry.getKey());
        prop.setValue(mapEntry.getValue());
        dest.getProperties().add(prop);
      }
      orderMessage.getDestinations().add(dest);
    }
    for (Map.Entry<String, String> mapEntry : order.getProperties().entrySet()) {
      Property prop = new Property();
      prop.setKey(mapEntry.getKey());
      prop.setValue(mapEntry.getValue());
      orderMessage.getProperties().add(prop);
    }
    return orderMessage;
  }
}
