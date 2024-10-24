// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.peripheral.loopback.commands;

import org.opentcs.commadapter.peripheral.loopback.LoopbackPeripheralCommAdapter;
import org.opentcs.drivers.peripherals.PeripheralAdapterCommand;
import org.opentcs.drivers.peripherals.PeripheralCommAdapter;

/**
 * A command to enable/disable the comm adapter's manual mode.
 */
public class EnableManualModeCommand
    implements
      PeripheralAdapterCommand {

  /**
   * Whether to enable/disable manual mode.
   */
  private final boolean enabled;

  /**
   * Creates a new instance.
   *
   * @param enabled Whether to enable/disable manual mode.
   */
  public EnableManualModeCommand(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void execute(PeripheralCommAdapter adapter) {
    if (!(adapter instanceof LoopbackPeripheralCommAdapter)) {
      return;
    }

    LoopbackPeripheralCommAdapter loopbackAdapter = (LoopbackPeripheralCommAdapter) adapter;
    loopbackAdapter.enableManualMode(enabled);
  }
}
