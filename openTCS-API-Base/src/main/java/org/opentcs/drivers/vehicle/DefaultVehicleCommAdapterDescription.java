/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;

/**
 * The default implementation of {@link VehicleCommAdapterDescription}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DefaultVehicleCommAdapterDescription
    extends VehicleCommAdapterDescription {

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
  public DefaultVehicleCommAdapterDescription(@Nonnull String description,
                                              boolean isSimVehicleCommAdapter) {
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
