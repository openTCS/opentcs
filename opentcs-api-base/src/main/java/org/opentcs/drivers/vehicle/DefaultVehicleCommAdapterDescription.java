// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;

/**
 * The default implementation of {@link VehicleCommAdapterDescription}.
 */
public class DefaultVehicleCommAdapterDescription
    extends
      VehicleCommAdapterDescription {

  /**
   * The description.
   */
  private final String description;
  /**
   * Whether the comm adapter is a simulating one.
   */
  private final boolean isSimVehicleCommAdapter;

  /**
   * Creates a new instance.
   *
   * @param description The description.
   * @param isSimVehicleCommAdapter Whether the comm adapter is a simulating one.
   */
  public DefaultVehicleCommAdapterDescription(
      @Nonnull
      String description,
      boolean isSimVehicleCommAdapter
  ) {
    this.description = requireNonNull(description, "description");
    this.isSimVehicleCommAdapter = isSimVehicleCommAdapter;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public boolean isSimVehicleCommAdapter() {
    return isSimVehicleCommAdapter;
  }
}
