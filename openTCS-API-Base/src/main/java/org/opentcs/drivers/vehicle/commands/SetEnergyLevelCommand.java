/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle.commands;

import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command to set a vehicle's energy level.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SetEnergyLevelCommand
    implements AdapterCommand {

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
    adapter.getProcessModel().setVehicleEnergyLevel(level);
  }
}
