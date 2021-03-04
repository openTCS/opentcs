/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.common;

/**
 * Defines the modes in which an application may be in.
 */
public enum ClientConnectionMode {
  /**
   * The application SHOULD be connected/online.
   */
  ONLINE,
  /**
   * The application SHOULD be disconnected/offline.
   */
  OFFLINE
}
