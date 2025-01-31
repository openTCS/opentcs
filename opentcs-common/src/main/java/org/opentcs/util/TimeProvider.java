// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util;

import java.time.Instant;

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
   * Returns the current time in milliseconds (i.e. the time that has passed since the epoch of
   * 1970-01-01T00:00:00Z).
   *
   * @return The current time in milliseconds.
   */
  public long getCurrentTimeEpochMillis() {
    return System.currentTimeMillis();
  }

  /**
   * Returns the current time as an {@link Instant}.
   *
   * @return The current time as an {@link Instant}.
   */
  public Instant getCurrentTimeInstant() {
    return Instant.now();
  }
}
