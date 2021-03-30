/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.peripherals;

import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * A command a peripheral communication adapter may execute.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface PeripheralAdapterCommand
    extends Serializable {

  /**
   * Executes the command.
   *
   * @param adapter The communication adapter to execute the command with.
   */
  void execute(@Nonnull PeripheralCommAdapter adapter);
}
