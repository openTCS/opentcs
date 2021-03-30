/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.peripheral.loopback.commands;

import org.opentcs.commadapter.peripheral.loopback.LoopbackPeripheralCommAdapter;
import org.opentcs.drivers.peripherals.PeripheralAdapterCommand;
import org.opentcs.drivers.peripherals.PeripheralCommAdapter;

/**
 * A command to pause/unpause the peripheral device.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SetPeripheralPausedCommand
    implements PeripheralAdapterCommand {

  /**
   * Whether to pause/unpause the peripheral device.
   */
  private final boolean paused;

  /**
   * Creates a new instance.
   *
   * @param paused Whether to pause/unpause the peripheral device.
   */
  public SetPeripheralPausedCommand(boolean paused) {
    this.paused = paused;
  }

  @Override
  public void execute(PeripheralCommAdapter adapter) {
    if (!(adapter instanceof LoopbackPeripheralCommAdapter)) {
      return;
    }

    LoopbackPeripheralCommAdapter loopbackAdapter = (LoopbackPeripheralCommAdapter) adapter;
    loopbackAdapter.pausePeripheral(paused);
  }
}
