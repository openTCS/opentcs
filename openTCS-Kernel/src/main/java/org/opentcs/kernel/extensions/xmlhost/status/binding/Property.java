/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.xmlhost.status.binding;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * A property of a Destination or a TransportOrder
 * 
 * @author Martin Grzenia (Fraunhofer IML)
 */
@XmlType(propOrder={"key", "value"})
public class Property {
  
  /**
   * The property key.
   */
  private String key = "";
  /**
   * The property value.
   */
  private String value = "";
  
  public Property() {
  }

  /**
   * Returns the property key.
   * 
   * @return The property key.
   */
  @XmlAttribute(name = "key", required = true)
  public String getKey() {
    return key;
  }

  /**
   * Sets the property key.
   * 
   * @param key The new key.
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * Returns the property value.
   * 
   * @return The property value.
   */
  @XmlAttribute(name = "value", required = true)
  public String getValue() {
    return value;
  }

  /**
   * Sets the property value.
   * 
   * @param value The new value.
   */
  public void setValue(String value) {
    this.value = value;
  }
}
