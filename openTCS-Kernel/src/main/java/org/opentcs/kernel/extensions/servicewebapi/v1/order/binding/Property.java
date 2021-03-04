/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.order.binding;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import static java.util.Objects.requireNonNull;

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

  public Property(String key, String value) {
    this.key = requireNonNull(key, "key");
    this.value = requireNonNull(value, "value");
  }

  /**
   * Returns the property key.
   *
   * @return The property key.
   */
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
