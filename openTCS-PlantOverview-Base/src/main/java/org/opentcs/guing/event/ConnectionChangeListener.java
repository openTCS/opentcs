/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.event;

/**
 * Interface for listeners that want to be informed on connection changes.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface ConnectionChangeListener {

  /**
   * Message from a connection that its components have changed.
   *
   * @param e The fired event.
   */
  void connectionChanged(ConnectionChangeEvent e);
}
