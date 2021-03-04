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
 * A response to a <code>TransportScript</code> request.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@XmlType(propOrder={"parsingSuccessful", "transports"})
public class ScriptResponse
extends TCSResponse {

  /**
   * Flag indicating whether parsing was successful.
   */
  private boolean parsingSuccessful;
  /**
   * A list of <code>TransportResponse</code>s.
   */
  private List<TransportResponse> transports = new LinkedList<>();
  
  /**
   * Creates a new instance.
   */
  public ScriptResponse() {
    // Do nada.
  }

  /**
   * Returns whether parsing was successful.
   * 
   * @return True if yes, false if not.
   */
  @XmlAttribute(name="parsingSuccessful", required=true)
  public boolean isParsingSuccessful() {
    return parsingSuccessful;
  }

  /**
   * Sets whether parsing was successful.
   * 
   * @param parsingSuccessful True if yes, false if not
   */
  public void setParsingSuccessful(boolean parsingSuccessful) {
    this.parsingSuccessful = parsingSuccessful;
  }

  /**
   * Returns the <code>TransportResponse</code>s.
   * 
   * @return The list.
   */
  @XmlElement(name="transport", required=false)
  public List<TransportResponse> getTransports() {
    return transports;
  }

  /**
   * Sets the list of <code>TransportResponse</code>s.
   * 
   * @param transports The list
   */
  public void setTransports(List<TransportResponse> transports) {
    this.transports = transports;
  }
}
