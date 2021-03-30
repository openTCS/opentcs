/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import java.util.List;
import static java.util.Objects.requireNonNull;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.Route;

/**
 * Computes routes between points.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface PointRouter {

  /**
   * A constant for marking the costs for a route as infinite.
   */
  long INFINITE_COSTS = Long.MAX_VALUE;

  /**
   * Returns a list of route steps to travel from a given source point to a given destination point.
   *
   * @param srcPoint The source point.
   * @param destPoint The destination point.
   * @return A list of steps in the order they are to be travelled from the source point to the
   * destination point.
   * The returned list does not include a step for the source point.
   * If source point and destination point are identical, the returned list will be empty.
   * If no route exists, <code>null</code> will be returned.
   */
  List<Route.Step> getRouteSteps(Point srcPoint, Point destPoint);

  /**
   * Returns the costs for travelling the shortest route from one point to another.
   *
   * @param srcPointRef The starting point reference.
   * @param destPointRef The destination point reference.
   * @return The costs for travelling the shortest route from the starting point to the destination
   * point.
   * If no route exists, {@link #INFINITE_COSTS INFINITE_COSTS} will be returned.
   */
  public long getCosts(TCSObjectReference<Point> srcPointRef,
                       TCSObjectReference<Point> destPointRef);

  /**
   * Returns the costs for travelling the shortest route from one point to another.
   *
   * @param srcPoint The starting point.
   * @param destPoint The destination point.
   * @return The costs for travelling the shortest route from the starting point to the destination
   * point.
   * If no route exists, {@link #INFINITE_COSTS INFINITE_COSTS} will be returned.
   */
  default long getCosts(Point srcPoint, Point destPoint) {
    requireNonNull(srcPoint, "srcPoint");
    requireNonNull(destPoint, "destPoint");

    return getCosts(srcPoint.getReference(), destPoint.getReference());
  }
}
