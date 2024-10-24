// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle.commands;

import jakarta.annotation.Nullable;
import org.opentcs.data.model.Triple;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command to set a vehicle's precise position.
 */
public class SetPrecisePositionCommand
    implements
      AdapterCommand {

  /**
   * The percise position to set.
   */
  private final Triple position;

  /**
   * Creates a new instance.
   *
   * @param position The precise position to set.
   */
  public SetPrecisePositionCommand(
      @Nullable
      Triple position
  ) {
    this.position = position;
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    adapter.getProcessModel().setPrecisePosition(position);
  }
}
