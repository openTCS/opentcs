// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle.commands;

import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.virtualvehicle.LoopbackCommunicationAdapter;

/**
 * A command to enable/disable the comm adapter's single step mode.
 */
public class SetSingleStepModeEnabledCommand
    implements
      AdapterCommand {

  /**
   * Whether to enable/disable single step mode.
   */
  private final boolean enabled;

  /**
   * Creates a new instance.
   *
   * @param enabled Whether to enable/disable single step mode.
   */
  public SetSingleStepModeEnabledCommand(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    if (!(adapter instanceof LoopbackCommunicationAdapter)) {
      return;
    }

    LoopbackCommunicationAdapter loopbackAdapter = (LoopbackCommunicationAdapter) adapter;
    loopbackAdapter.getProcessModel().setSingleStepModeEnabled(enabled);
  }

}
