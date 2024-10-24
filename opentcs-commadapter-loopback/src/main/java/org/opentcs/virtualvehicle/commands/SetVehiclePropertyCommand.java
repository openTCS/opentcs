// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle.commands;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command to set a vehicle's property.
 */
public class SetVehiclePropertyCommand
    implements
      AdapterCommand {

  /**
   * The property key to set.
   */
  private final String key;
  /**
   * The property value to set.
   */
  private final String value;

  /**
   * Creates a new instance.
   *
   * @param key The property key to set.
   * @param value The property value to set.
   */
  public SetVehiclePropertyCommand(
      @Nonnull
      String key,
      @Nullable
      String value
  ) {
    this.key = requireNonNull(key, "key");
    this.value = value;
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    adapter.getProcessModel().setProperty(key, value);
  }
}
