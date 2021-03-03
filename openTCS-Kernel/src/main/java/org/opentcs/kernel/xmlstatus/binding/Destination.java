/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.kernel.xmlstatus.binding;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import org.opentcs.data.order.DriveOrder;

/**
 * A {@link org.opentcs.data.order.DriveOrder DriveOrder}'s destination.
 */
public class Destination {

  /**
   * The name of the destination location.
   */
  private String locationName = "";
  /**
   * The operation to be executed at the destination location.
   */
  private String operation = "";
  /**
   * The <code>DriveOrder</code>'s state.
   */
  private DriveOrder.State state;
  /**
   * The <code>DriveOrder</code>'s properties.
   */
  private List<Property> properties = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public Destination() {
  }

  /**
   * Returns the name of the destination location.
   *
   * @return The name of the destination location.
   */
  @XmlAttribute(name = "locationName", required = true)
  public String getLocationName() {
    return locationName;
  }

  /**
   * Sets the name of the destination location.
   *
   * @param name The name of the destination location.
   */
  public void setLocationName(String name) {
    this.locationName = Objects.requireNonNull(name, "name");
  }

  /**
   * Returns the operation to be executed at the destination location.
   *
   * @return The operation to be executed at the destination location.
   */
  @XmlAttribute(name = "operation", required = true)
  public String getOperation() {
    return operation;
  }

  /**
   * Sets the operation to be executed at the destination location.
   *
   * @param operation The operation to be executed at the destination
   * location.
   */
  public void setOperation(String operation) {
    this.operation = Objects.requireNonNull(operation, "operation");
  }

  /**
   * Returns the <code>DriveOrder</code>'s state.
   *
   * @return The <code>DriveOrder</code>'s state.
   */
  @XmlAttribute(name = "state", required = true)
  public DriveOrder.State getState() {
    return state;
  }

  /**
   * Sets the <code>DriveOrder</code>'s state.
   *
   * @param state The <code>DriveOrder</code>'s state.
   */
  public void setState(DriveOrder.State state) {
    this.state = Objects.requireNonNull(state, "state");
  }

  @XmlElement(name = "property", required = false)
  public List<Property> getProperties() {
    return properties;
  }

  public void setProperties(List<Property> properties) {
    this.properties = properties;
  }
}
