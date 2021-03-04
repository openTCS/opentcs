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
 * A command to reset a vehicle's position.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ResetPositionCommand
    implements AdapterCommand {

  @Override
  public void execute(VehicleCommAdapter adapter) {
    adapter.getProcessModel().setVehiclePosition(null);
  }
}
