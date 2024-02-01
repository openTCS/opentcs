/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.time.Instant;
import javax.inject.Inject;

/**
 * Keeps track of the time used to determine whether a working set item should be removed (according
 * to its creation time).
 */
public class CreationTimeThreshold {

  private Instant currentThreshold;

  /**
   * Creates a new instance.
   */
  @Inject
  public CreationTimeThreshold() {
  }

  /**
   * Updates the current threshold by subtracting the given amount of milliseconds from the current
   * time.
   *
   * @param millis The amount of milliseconds to subtract from the current time.
   */
  public void updateCurrentThreshold(long millis) {
    currentThreshold = Instant.now().minusMillis(millis);
  }

  /**
   * Returns the current threshold.
   * <p>
   * Working set items that are created before this point of time should be removed.
   *
   * @return The current threshold.
   */
  public Instant getCurrentThreshold() {
    return currentThreshold;
  }
}
