// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
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
