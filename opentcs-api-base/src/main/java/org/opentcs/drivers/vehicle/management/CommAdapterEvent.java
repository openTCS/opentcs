// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle.management;

import java.io.Serializable;
import org.opentcs.drivers.LowLevelCommunicationEvent;

/**
 * Instances of this class represent events emitted by/for comm adapter changes.
 */
public abstract class CommAdapterEvent
    implements
      LowLevelCommunicationEvent,
      Serializable {

  /**
   * Creates an empty event.
   */
  public CommAdapterEvent() {
  }
}
