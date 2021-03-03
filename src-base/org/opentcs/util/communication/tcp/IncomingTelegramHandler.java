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
 * Implemented by classes that are capable of handling telegrams received from
 * a peer.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface IncomingTelegramHandler {
  /**
   * Handles a telegram from the peer.
   *
   * @param telegram The telegram to be handled.
   */
  void handleTelegram(IncomingTelegram telegram);
}
