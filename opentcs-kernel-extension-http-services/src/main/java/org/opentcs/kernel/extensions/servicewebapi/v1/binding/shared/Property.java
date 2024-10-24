// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * A key-value property.
 */
public class Property {

  private String key;

  private String value;

  public Property(
      @Nonnull
      @JsonProperty(required = true, value = "key")
      String key,
      @Nullable
      @JsonProperty(required = false, value = "value")
      String value
  ) {
    this.key = requireNonNull(key, "key");
    this.value = value;
  }

  /**
   * Returns the property key.
   *
   * @return The property key.
   */
  @Nonnull
  public String getKey() {
    return key;
  }

  /**
   * Sets the property key.
   *
   * @param key The new key.
   */
  public void setKey(
      @Nonnull
      String key
  ) {
    this.key = requireNonNull(key, "key");
  }

  /**
   * Returns the property value.
   *
   * @return The property value.
   */
  @Nullable
  public String getValue() {
    return value;
  }

  /**
   * Sets the property value.
   *
   * @param value The new value.
   */
  public void setValue(
      @Nullable
      String value
  ) {
    this.value = value;
  }
}
