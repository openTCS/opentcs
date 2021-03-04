/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.xmlhost.orders.binding;

import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A destination of a transport.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@XmlType(propOrder = {"locationName", "operation", "properties"})
public class Destination {

  /**
   * The location name.
   */
  private String locationName = "";
  /**
   * The operation.
   */
  private String operation = "";
  /**
   * The destination's properties.
   */
  private List<Property> properties = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public Destination() {
    // Do nada.
  }

  /**
   * Returns the location name.
   *
   * @return The location name
   */
  @XmlAttribute(name = "locationName", required = true)
  public String getLocationName() {
    return locationName;
  }

  /**
   * Sets the location name.
   *
   * @param locationName The new name
   */
  public void setLocationName(String locationName) {
    this.locationName = locationName;
  }

  /**
   * Returns the operation.
   *
   * @return The operation
   */
  @XmlAttribute(name = "operation", required = true)
  public String getOperation() {
    return operation;
  }

  /**
   * Sets the operation.
   *
   * @param operation The new operation
   */
  public void setOperation(String operation) {
    this.operation = operation;
  }

  /**
   * Returns the destination's properties.
   *
   * @return The destination's properties.
   */
  @XmlElement(name = "property", required = false)
  public List<Property> getProperties() {
    return properties;
  }

  /**
   * Sets the destination's properties.
   *
   * @param properties The new destination's properties.
   */
  public void setProperties(List<Property> properties) {
    this.properties = properties;
  }
}
