/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
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
