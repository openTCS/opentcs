// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.event;

/**
 * Interface for listeners that want to be informed on connection changes.
 */
public interface ConnectionChangeListener {

  /**
   * Message from a connection that its components have changed.
   *
   * @param e The fired event.
   */
  void connectionChanged(ConnectionChangeEvent e);
}
