/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.communication.tcp;

/**
 * Implemented by classes that wish to be notified when the state of a
 * connection changed.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ConnectionStateListener {
  /**
   * Called when the state of a <code>TcpConnection</code> changed, upon connect
   * or disconnect.
   *
   * @param connection the connection whose state has changed.
   * @param connected <code>true</code> if the connection's state changed from
   * disconnected to connected; <code>false</code> if its state changed from
   * connected to disconnected.
   */
  void connectionStateChanged(TcpConnection connection, boolean connected);
}
