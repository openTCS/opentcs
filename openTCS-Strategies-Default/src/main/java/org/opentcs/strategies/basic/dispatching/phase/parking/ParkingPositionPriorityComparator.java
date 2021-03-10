/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.parking;

import java.util.Comparator;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.data.model.Point;

/**
 * Compares parking positions by their priorities.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ParkingPositionPriorityComparator
    implements Comparator<Point> {

  /**
   * A function computing the priority of a parking position.
   */
  private final ParkingPositionToPriorityFunction priorityFunction;

  /**
   * Creates a new instance.
   *
   * @param priorityFunction A function computing the priority of a parking position.
   */
  @Inject
  public ParkingPositionPriorityComparator(ParkingPositionToPriorityFunction priorityFunction) {
    this.priorityFunction = requireNonNull(priorityFunction, "priorityFunction");
  }

  @Override
  public int compare(Point point1, Point point2) {
    requireNonNull(point1, "point1");
    requireNonNull(point2, "point2");

    Integer point1Prio = priorityFunction.apply(point1);
    Integer point2Prio = priorityFunction.apply(point2);

    if (point1Prio != null && point2Prio == null) {
      return -1;
    }
    else if (point1Prio == null && point2Prio != null) {
      return 1;
    }
    else if (point1Prio == null && point2Prio == null) {
      return point1.getName().compareTo(point2.getName());
    }
    return point1Prio.compareTo(point2Prio);
  }
}
