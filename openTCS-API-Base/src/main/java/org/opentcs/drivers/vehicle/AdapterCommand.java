/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import java.io.Serializable;

/**
 * A command a comm adapter may execute.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface AdapterCommand
    extends Serializable {

  /**
   * Executes the command.
   *
   * @param adapter The comm adapter to execute the command with.
   */
  void execute(VehicleCommAdapter adapter);
}
