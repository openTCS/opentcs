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
 * Utility methods for formatting time periods.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
final class TimePeriodFormat {

  /**
   * Prevents undesired instantiation.
   */
  private TimePeriodFormat() {
    // Do nada.
  }

  /**
   * Returns the given duration in milliseconds formatted as a human readable
   * string.
   *
   * @param milliseconds The duration to be formatted in milliseconds.
   * @return The given duration in milliseconds formatted as a human readable
   * string.
   */
  public static String formatHumanReadable(final long milliseconds) {
    if (milliseconds < 0) {
      throw new IllegalArgumentException("milliseconds is negative: "
          + milliseconds);
    }

    long ms = milliseconds;

    final long hours = ms / (1000 * 60 * 60);
    ms -= hours * (1000 * 60 * 60);

    final long minutes = ms / (1000 * 60);
    ms -= minutes * (1000 * 60);

    final long seconds = ms / 1000;
    ms -= seconds * 1000;

    StringBuilder sb = new StringBuilder();
    boolean mustAdd = false;
    if (hours > 0) {
      sb.append(hours).append("h ");
      mustAdd = true;
    }
    if (mustAdd || minutes > 0) {
      sb.append(minutes).append("min ");
      mustAdd = true;
    }
    sb.append(seconds).append("s");
    return sb.toString();
  }
}
