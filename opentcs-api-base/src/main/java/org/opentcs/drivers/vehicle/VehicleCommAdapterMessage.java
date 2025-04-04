// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.util.Map;

/**
 * A message a comm adapter may process.
 */
public class VehicleCommAdapterMessage
    implements
      Serializable {

  private final String type;
  private final Map<String, String> parameters;

  /**
   * Creates a new instance.
   *
   * @param type The message's type.
   * @param parameters The message's parameters.
   */
  public VehicleCommAdapterMessage(
      @Nonnull
      String type,
      @Nonnull
      Map<String, String> parameters
  ) {
    this.type = requireNonNull(type, "type");
    this.parameters = requireNonNull(parameters, "parameters");
  }

  /**
   * Returns the message's type.
   *
   * @return The message's type.
   */
  @Nonnull
  public String getType() {
    return type;
  }

  /**
   * Returns the message's parameters.
   *
   * @return The message's parameters.
   */
  @Nonnull
  public Map<String, String> getParameters() {
    return parameters;
  }
}
