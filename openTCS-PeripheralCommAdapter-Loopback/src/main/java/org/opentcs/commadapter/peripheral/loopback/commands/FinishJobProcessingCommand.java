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
 * A command to trigger the comm adapter in manual mode and finish the job the simulated peripheral
 * device is currently processing.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class FinishJobProcessingCommand
    implements PeripheralAdapterCommand {

  /**
   * Whether to fail the execution of the job the (simulated) peripheral device is currently
   * processing.
   */
  private final boolean failJob;

  /**
   * Creates a new instance.
   *
   * @param failJob Whether to fail the execution of the job the (simulated) peripheral device is
   * currently processing.
   */
  public FinishJobProcessingCommand(boolean failJob) {
    this.failJob = failJob;
  }

  @Override
  public void execute(PeripheralCommAdapter adapter) {
    if (!(adapter instanceof LoopbackPeripheralCommAdapter)) {
      return;
    }

    LoopbackPeripheralCommAdapter loopbackAdapter = (LoopbackPeripheralCommAdapter) adapter;
    loopbackAdapter.triggerJobProcessing(failJob);
  }
}
