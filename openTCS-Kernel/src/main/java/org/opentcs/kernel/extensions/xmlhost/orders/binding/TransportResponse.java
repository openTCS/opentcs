/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.xmlhost.orders.binding;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * A response to a <code>Transport</code> request.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@XmlType(propOrder={"executionSuccessful", "orderName"})
public class TransportResponse
extends TCSResponse {

  /**
   * Flag indicating whether execution was successful.
   */
  private boolean executionSuccessful = true;
  /**
   * The orders name.
   */
  private String orderName = "";
  
  /**
   * Creates a new instance.
   */
  public TransportResponse() {
    // Do nada.
  }

  /**
   * Returns whether execution was successful.
   * 
   * @return True if yes, false if not.
   */
  @XmlAttribute(name="executionSuccessful", required=true)
  public boolean isExecutionSuccessful() {
    return executionSuccessful;
  }

  /**
   * Sets whether execution was successful.
   * 
   * @param executionSuccessful True if yes, false if not.
   */
  public void setExecutionSuccessful(boolean executionSuccessful) {
    this.executionSuccessful = executionSuccessful;
  }

  /**
   * Returns this orders name.
   * 
   * @return The name.
   */
  @XmlAttribute(name="orderName", required=true)
  public String getOrderName() {
    return orderName;
  }

  /**
   * Sets this orders name.
   * 
   * @param orderName The new name.
   */
  public void setOrderName(String orderName) {
    this.orderName = orderName;
  }
}
