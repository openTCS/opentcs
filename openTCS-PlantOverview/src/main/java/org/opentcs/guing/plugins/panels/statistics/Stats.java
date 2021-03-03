/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.statistics;

import java.util.Objects;

/**
 * Statistics data for an object.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
abstract class Stats {

  /**
   * The name of the object.
   */
  private final String name;
  /**
   * The total runtime recorded.
   */
  private final long totalRuntime;

  /**
   * Creates a new instance.
   *
   * @param name The name of the object.
   * @param totalRuntime The total runtime recorded.
   */
  Stats(final String name, final long totalRuntime) {
    this.name = Objects.requireNonNull(name, "name is null");
    if (totalRuntime < 0) {
      throw new IllegalArgumentException("totalRuntime not positive: "
          + totalRuntime);
    }
    if (totalRuntime == 0) {
      this.totalRuntime = 1;
    }
    else {
      this.totalRuntime = totalRuntime;
    }
  }

  /**
   * Returns the name of the object.
   *
   * @return The name of the object.
   */
  public final String getName() {
    return name;
  }

  /**
   * Returns the total runtime recorded.
   *
   * @return The total runtime recorded.
   */
  public final long getTotalRuntime() {
    return totalRuntime;
  }
}
