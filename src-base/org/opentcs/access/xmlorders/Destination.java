/*
 * openTCS copyright information:
 * Copyright (c) 2009 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.xmlorders;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * A destination of a transport.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@XmlType(propOrder={"locationName", "operation"})
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
}
