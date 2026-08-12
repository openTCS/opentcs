// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common.mqtt;

import javax.annotation.Nonnull;

/**
 * A listener for events concerning an established connection.
 */
public interface ConnectionEventListener {

  /**
   * Called when a message from the remote peer has been received and decoded.
   *
   * @param message The incoming message.
   */
  void onIncomingMessage(
      @Nonnull
      IncomingMessage message
  );

  /**
   * Called when a connection to the remote peer has been established.
   */
  void onConnect();

  /**
   * Called when a connection attempt to the remote peer has failed.
   */
  void onFailedConnectionAttempt();

  /**
   * Called when a connection to the remote peer has been closed (by either side).
   */
  void onDisconnect();

  /**
   * Called when the remote peer is considered idle.
   */
  void onIdle();
}
