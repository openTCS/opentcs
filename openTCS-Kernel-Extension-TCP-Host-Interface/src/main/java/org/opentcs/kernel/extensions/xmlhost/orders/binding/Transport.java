/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.xmlhost.orders.binding;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A transport order to be processed by the kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@XmlType(propOrder = {"deadline", "intendedVehicle", "destinations", "properties", "dependencies"})
public class Transport
    extends TCSOrder {

  /**
   * The deadling of this transport order.
   */
  private Date deadline;
  /**
   * The intended vehicle.
   */
  private String intendedVehicle;
  /**
   * The destinations.
   */
  private List<Destination> destinations = new LinkedList<>();
  /**
   * The transport order's properties.
   */
  private List<Property> properties = new LinkedList<>();
  /**
   * The dependencies.
   */
  private List<String> dependencies = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public Transport() {
    // Do nada.
  }

  /**
   * Returns the deadline.
   *
   * @return The deadline.
   */
  @XmlAttribute(name = "deadline", required = false)
  public Date getDeadline() {
    return deadline;
  }

  /**
   * Sets the deadline.
   *
   * @param deadline The new deadline.
   */
  public void setDeadline(Date deadline) {
    this.deadline = deadline;
  }

  /**
   * Returns the intended vehicle.
   *
   * @return The intended vehicle.
   */
  @XmlAttribute(name = "intendedVehicle", required = false)
  public String getIntendedVehicle() {
    return intendedVehicle;
  }

  /**
   * Sets the intended vehicle.
   *
   * @param intendedVehicle The new intended vehicle.
   */
  public void setIntendedVehicle(String intendedVehicle) {
    this.intendedVehicle = intendedVehicle;
  }

  /**
   * Returns the destinations.
   *
   * @return The list of destinations.
   */
  @XmlElement(name = "destination", required = true)
  public List<Destination> getDestinations() {
    return destinations;
  }

  /**
   * Sets the destinations.
   *
   * @param destinations The new list of destinations.
   */
  public void setDestinations(List<Destination> destinations) {
    this.destinations = destinations;
  }

  /**
   * Returns the transport order's properties.
   *
   * @return The transport order's properties.
   */
  @XmlElement(name = "property", required = false)
  public List<Property> getProperties() {
    return properties;
  }

  /**
   * Sets the transport order's properties.
   *
   * @param properties The new transport order's properties.
   */
  public void setProperties(List<Property> properties) {
    this.properties = properties;
  }

  /**
   * Returns the dependencies.
   *
   * @return The list of dependencies.
   */
  @XmlElement(name = "dependency", required = false)
  public List<String> getDependencies() {
    return dependencies;
  }

  /**
   * Sets the dependencies.
   *
   * @param dependencies The new list of dependencies.
   */
  public void setDependencies(List<String> dependencies) {
    this.dependencies = dependencies;
  }
}
