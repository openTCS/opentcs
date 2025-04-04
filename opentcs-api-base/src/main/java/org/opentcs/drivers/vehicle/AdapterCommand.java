// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle;

import java.io.Serializable;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A command a comm adapter may execute.
 *
 * @deprecated Use {@link VehicleCommAdapterMessage} instead.
 */
@Deprecated
@ScheduledApiChange(when = "7.0", details = "Will be removed.")
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
