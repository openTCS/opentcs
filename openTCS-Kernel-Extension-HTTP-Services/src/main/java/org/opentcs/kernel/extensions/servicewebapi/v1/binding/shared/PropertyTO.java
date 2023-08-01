/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;

/**
 */
public class PropertyTO {

  private String name;
  private String value;

  @JsonCreator
  public PropertyTO(@Nonnull @JsonProperty(value = "name", required = true) String name,
                    @Nonnull @JsonProperty(value = "value", required = true) String value) {
    this.name = requireNonNull(name, "name");
    this.value = requireNonNull(value, "value");
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public PropertyTO setName(@Nonnull String name) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  @Nonnull
  public String getValue() {
    return value;
  }

  public PropertyTO setValue(@Nonnull String value) {
    this.value = requireNonNull(value, "value");
    return this;
  }

}
