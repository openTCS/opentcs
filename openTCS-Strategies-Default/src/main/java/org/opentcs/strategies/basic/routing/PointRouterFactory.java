/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import java.util.Set;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Implementations of this interface construct point routers.
 */
public interface PointRouterFactory {

  /**
   * Creates a point router for the given vehicle while excluding the given set of points and paths
   * from it.
   *
   * @param vehicle The vehicle.
   * @param pointsToExclude The set of points to be excluded.
   * @param pathsToExclude The set of paths to be excluded.
   * @return The point router.
   */
  PointRouter createPointRouter(Vehicle vehicle,
                                Set<Point> pointsToExclude,
                                Set<Path> pathsToExclude);
}
