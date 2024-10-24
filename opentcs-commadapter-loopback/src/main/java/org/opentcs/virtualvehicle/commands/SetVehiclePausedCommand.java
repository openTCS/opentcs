// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle.commands;

import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.virtualvehicle.LoopbackCommunicationAdapter;

/**
 * A command to pause/unpause the vehicle.
 */
public class SetVehiclePausedCommand
    implements
      AdapterCommand {

  /**
   * Whether to pause/unpause the vehicle.
   */
  private final boolean paused;

  /**
   * Creates a new instance.
   *
   * @param paused Whether to pause/unpause the vehicle.
   */
  public SetVehiclePausedCommand(boolean paused) {
    this.paused = paused;
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    if (!(adapter instanceof LoopbackCommunicationAdapter)) {
      return;
    }

    LoopbackCommunicationAdapter loopbackAdapter = (LoopbackCommunicationAdapter) adapter;
    loopbackAdapter.getProcessModel().setVehiclePaused(paused);
  }

}
