// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.watchdog;

/**
 * Provides the current time.
 */
public class TimeProvider {

  /**
   * Creates a new instance.
   */
  public TimeProvider() {
  }

  /**
   * Returns the current time.
   *
   * @return The current time.
   */
  public long getCurrentTime() {
    return System.currentTimeMillis();
  }
}
