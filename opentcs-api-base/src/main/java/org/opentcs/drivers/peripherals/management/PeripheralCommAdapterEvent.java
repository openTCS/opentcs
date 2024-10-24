// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.peripherals.management;

import java.io.Serializable;
import org.opentcs.drivers.LowLevelCommunicationEvent;

/**
 * Instances of this class represent events emitted by/for peripheral comm adapter changes.
 */
public abstract class PeripheralCommAdapterEvent
    implements
      LowLevelCommunicationEvent,
      Serializable {

  /**
   * Creates an empty event.
   */
  public PeripheralCommAdapterEvent() {
  }
}
