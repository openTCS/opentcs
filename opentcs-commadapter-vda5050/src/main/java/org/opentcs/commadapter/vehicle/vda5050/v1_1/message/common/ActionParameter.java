// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Additional parameters for an {@link Action}.
 */
public class ActionParameter
    implements
      Serializable {

  /**
   * The key of the action parameter.
   */
  private String key;
  /**
   * The value of the action parameter.
   * Can be an array, boolean, number or string.
   */
  private Object value;

  @JsonCreator
  public ActionParameter(
      @Nonnull
      @JsonProperty(required = true, value = "key")
      String key,
      @Nonnull
      @JsonProperty(required = true, value = "value")
      Object value
  ) {
    this.key = requireNonNull(key, "key");
    this.value = requireNonNull(value, "value");
  }

  public String getKey() {
    return key;
  }

  public ActionParameter setKey(
      @Nonnull
      String key
  ) {
    this.key = requireNonNull(key, "key");
    return this;
  }

  public Object getValue() {
    return value;
  }

  public ActionParameter setValue(
      @Nonnull
      Object value
  ) {
    this.value = requireNonNull(value, "value");
    return this;
  }

  @Override
  public String toString() {
    return "ActionParameter{" + "key=" + key + ", value=" + value + '}';
  }

}
