// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.peripherals;

import jakarta.annotation.Nonnull;
import java.io.Serializable;

/**
 * A command a peripheral communication adapter may execute.
 */
public interface PeripheralAdapterCommand
    extends
      Serializable {

  /**
   * Executes the command.
   *
   * @param adapter The communication adapter to execute the command with.
   */
  void execute(
      @Nonnull
      PeripheralCommAdapter adapter
  );
}
