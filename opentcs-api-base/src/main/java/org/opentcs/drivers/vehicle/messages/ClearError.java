// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle.messages;

import java.io.Serializable;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A message that informs a communication adapter that it/the vehicle should
 * reset currently active errors if possible.
 *
 * @deprecated Use communication adapter-specific
 * {@link org.opentcs.drivers.vehicle.VehicleCommAdapterMessage}s instead.
 */
@Deprecated
@ScheduledApiChange(when = "7.0", details = "Will be removed.")
public class ClearError
    implements
      Serializable {

  /**
   * Creates a new instance.
   */
  public ClearError() {
    // Do nada.
  }
}
