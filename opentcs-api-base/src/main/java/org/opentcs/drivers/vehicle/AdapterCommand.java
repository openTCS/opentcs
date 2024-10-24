// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle;

import java.io.Serializable;

/**
 * A command a comm adapter may execute.
 */
public interface AdapterCommand
    extends
      Serializable {

  /**
   * Executes the command.
   *
   * @param adapter The comm adapter to execute the command with.
   */
  void execute(VehicleCommAdapter adapter);
}
