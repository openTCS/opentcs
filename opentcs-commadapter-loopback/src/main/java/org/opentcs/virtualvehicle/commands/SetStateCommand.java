// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle.commands;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command to set a vehicle's state.
 */
public class SetStateCommand
    implements
      AdapterCommand {

  /**
   * The vehicle state to set.
   */
  private final Vehicle.State state;

  /**
   * Creates a new instance.
   *
   * @param state The vehicle state to set.
   */
  public SetStateCommand(
      @Nonnull
      Vehicle.State state
  ) {
    this.state = requireNonNull(state, "state");
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    adapter.getProcessModel().setState(state);
  }
}
