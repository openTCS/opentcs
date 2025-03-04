// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.peripherals;

import jakarta.annotation.Nonnull;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.drivers.peripherals.PeripheralAdapterCommand;
import org.opentcs.drivers.peripherals.PeripheralCommAdapter;
import org.opentcs.drivers.peripherals.PeripheralJobCallback;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;
import org.opentcs.util.ExplainedBoolean;

/**
 * A {@link PeripheralCommAdapter} implementation that is doing nothing.
 */
public class NullPeripheralCommAdapter
    implements
      PeripheralCommAdapter {

  /**
   * The process model.
   */
  private final PeripheralProcessModel processModel;

  /**
   * Creates a new instance.
   *
   * @param location The reference to the location this adapter is attached to.
   */
  public NullPeripheralCommAdapter(
      @Nonnull
      TCSResourceReference<Location> location
  ) {
    this.processModel = new PeripheralProcessModel(location);
  }

  @Override
  public void initialize() {
  }

  @Override
  public boolean isInitialized() {
    return true;
  }

  @Override
  public void terminate() {
  }

  @Override
  public void enable() {
  }

  @Override
  public void disable() {
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public PeripheralProcessModel getProcessModel() {
    return processModel;
  }

  @Override
  public ExplainedBoolean canProcess(PeripheralJob job) {
    return new ExplainedBoolean(false, "Can't process any jobs.");
  }

  @Override
  public void process(PeripheralJob job, PeripheralJobCallback callback) {
  }

  @Override
  public void abortJob() {
  }

  @Override
  public void execute(PeripheralAdapterCommand command) {
  }
}
