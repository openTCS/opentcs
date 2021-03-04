/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
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
