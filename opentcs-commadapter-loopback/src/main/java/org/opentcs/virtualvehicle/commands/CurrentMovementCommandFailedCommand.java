// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle.commands;

import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command to notify the loopback adapter the last/current movement command failed.
 */
public class CurrentMovementCommandFailedCommand
    implements
      AdapterCommand {

  /**
   * Creates a new instance.
   */
  public CurrentMovementCommandFailedCommand() {
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    MovementCommand failedCommand = adapter.getSentCommands().peek();
    if (failedCommand != null) {
      adapter.getProcessModel().commandFailed(adapter.getSentCommands().peek());
    }
  }
}
