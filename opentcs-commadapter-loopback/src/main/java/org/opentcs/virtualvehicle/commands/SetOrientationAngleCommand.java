// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle.commands;

import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command to set a vehicle's orientation angle.
 */
public class SetOrientationAngleCommand
    implements
      AdapterCommand {

  /**
   * The orientation angle to set.
   */
  private final double angle;

  /**
   * Creates a new instance.
   *
   * @param angle The orientation angle to set.
   */
  public SetOrientationAngleCommand(double angle) {
    this.angle = angle;
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    adapter.getProcessModel().setPose(
        adapter.getProcessModel().getPose().withOrientationAngle(angle)
    );
  }
}
