/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Instances of this class provide a way to acquire unique timestamps, working
 * around the limitations of System.currentTimeMillis().
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Code should be able to cope with non-unique time stamps.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
public class UniqueTimestampGenerator {

  /**
   * The last timestamp returned by getNextTimestamp().
   */
  private long lastTimestamp;

  /**
   * Creates a new UniqueTimestampGenerator.
   */
  public UniqueTimestampGenerator() {
  }

  /**
   * Returns a unique timestamp for each call.
   * This method first acquires the current system time via
   * <code>System.currentTimeMillis()</code>. It then checks if that value
   * succeeds the value the method returned the previous time; if it does not,
   * the previous value is incremented by 1 and returned, otherwise the return
   * value of <code>System.currentTimeMillis()</code> is returned unchanged.
   * This works around the limited granularity of
   * <code>System.currentTimeMillis()</code> to ensure a unique timestamp is
   * returned for every call of this method.
   *
   * @return A unique timestamp.
   */
  public synchronized long getNextTimestamp() {
    long timeStamp = System.currentTimeMillis();
    if (timeStamp <= lastTimestamp) {
      timeStamp = lastTimestamp + 1;
    }
    lastTimestamp = timeStamp;
    return timeStamp;
  }
}
