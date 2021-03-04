/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle.commands;

import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.virtualvehicle.LoopbackCommunicationAdapter;

/**
 * A command to trigger the comm adapter in single step mode.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class TriggerCommand
    implements AdapterCommand {

  @Override
  public void execute(VehicleCommAdapter adapter) {
    if (!(adapter instanceof LoopbackCommunicationAdapter)) {
      return;
    }

    LoopbackCommunicationAdapter loopbackAdapter = (LoopbackCommunicationAdapter) adapter;
    loopbackAdapter.trigger();
  }
}
