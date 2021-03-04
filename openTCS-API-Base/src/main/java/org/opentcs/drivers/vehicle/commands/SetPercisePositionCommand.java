/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle.commands;

import javax.annotation.Nullable;
import org.opentcs.data.model.Triple;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command to set a vehicle's percise position.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SetPercisePositionCommand
    implements AdapterCommand {

  /**
   * The percise position to set.
   */
  private final Triple position;

  /**
   * Creates a new instance.
   *
   * @param position The percise position to set.
   */
  public SetPercisePositionCommand(@Nullable Triple position) {
    this.position = position;
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    adapter.getProcessModel().setVehiclePrecisePosition(position);
  }
}
