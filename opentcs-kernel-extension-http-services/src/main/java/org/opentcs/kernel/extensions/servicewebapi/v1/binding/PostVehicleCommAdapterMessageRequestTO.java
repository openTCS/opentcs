// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import java.util.List;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * A message to be sent to a vehicle driver.
 */
public class PostVehicleCommAdapterMessageRequestTO {

  private final String type;
  private final List<Property> parameters;

  @JsonCreator
  public PostVehicleCommAdapterMessageRequestTO(
      @Nonnull
      @JsonProperty(value = "type", required = true)
      String type,
      @Nonnull
      @JsonProperty(value = "parameters", required = true)
      List<Property> parameters
  ) {
    this.type = requireNonNull(type, "type");
    this.parameters = requireNonNull(parameters, "parameters");
  }

  @Nonnull
  public String getType() {
    return type;
  }

  @Nonnull
  public List<Property> getParameters() {
    return parameters;
  }
}
