/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.status.binding;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * A property of a Destination or a TransportOrder
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class Property {

  @JsonProperty(required = true)
  @JsonPropertyDescription("The property's key")
  private String key = "";

  @JsonProperty(required = true)
  @JsonPropertyDescription("The property's value")
  private String value = "";

  public Property() {
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
