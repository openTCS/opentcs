// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle.commands;

import jakarta.annotation.Nullable;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command to set a vehicle's position.
 */
public class SetPositionCommand
    implements
      AdapterCommand {

  /**
   * The position to set.
   */
  private final String position;

  /**
   * Creates a new instance.
   *
   * @param position The position to set.
   */
  public SetPositionCommand(
      @Nullable
      String position
  ) {
    this.position = position;
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    adapter.getProcessModel().setPosition(position);
  }
}
