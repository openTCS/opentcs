/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.statistics;

/**
 * Statistics data for a point.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class PointStats
    extends Stats {

  /**
   * The total time the point was occupied by vehicles.
   */
  private long totalTimeOccupied;
  /**
   * When the point became occupied the last time.
   */
  private long lastOccupationStart;

  /**
   * Creates a new instance.
   *
   * @param name The name of the point.
   * @param totalRuntime The total runtime recorded.
   */
  public PointStats(final String name, final long totalRuntime) {
    super(name, totalRuntime);
  }

  /**
   * Returns the total time the point was occupied by vehicles.
   *
   * @return The total time the point was occupied by vehicles.
   */
  public long getTotalTimeOccupied() {
    return totalTimeOccupied;
  }

  /**
   * Indicates the point became occupied at the given point of time.
   *
   * @param timestamp When the point became occupied.
   */
  public void startOccupation(long timestamp) {
    assert timestamp > 0;
    lastOccupationStart = timestamp;
  }

  /**
   * Indicates the point became free at the given point of time.
   *
   * @param timestamp When the point became free.
   */
  public void stopOccupation(long timestamp) {
    assert timestamp > 0;
    if (lastOccupationStart != 0) {
      totalTimeOccupied += timestamp - lastOccupationStart;
    }
    lastOccupationStart = 0;
  }
}
