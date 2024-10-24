// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle.messages;

import java.io.Serializable;

/**
 * A message that informs a communication adapter that it/the vehicle should
 * reset currently active errors if possible.
 */
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
