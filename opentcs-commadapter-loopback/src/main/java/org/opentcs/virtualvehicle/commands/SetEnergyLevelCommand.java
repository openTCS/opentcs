// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle.commands;

import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command to set a vehicle's energy level.
 */
public class SetEnergyLevelCommand
    implements
      AdapterCommand {

  /**
   * The energy level to set.
   */
  private final int level;

  /**
   * Creates a new instance.
   *
   * @param level The energy level to set.
   */
  public SetEnergyLevelCommand(int level) {
    this.level = level;
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    adapter.getProcessModel().setEnergyLevel(level);
  }
}
