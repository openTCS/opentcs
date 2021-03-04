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
 * A command to set a vehicle's orientation angle.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SetOrienatationAngleCommand
    implements AdapterCommand {

  /**
   * The orientation angle to set.
   */
  private final double angle;

  /**
   * Creates a new instance.
   *
   * @param angle The orientation angle to set.
   */
  public SetOrienatationAngleCommand(double angle) {
    this.angle = angle;
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    adapter.getProcessModel().setVehicleOrientationAngle(angle);
  }
}
