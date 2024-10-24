// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
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
  PointRouter createPointRouter(
      Vehicle vehicle,
      Set<Point> pointsToExclude,
      Set<Path> pathsToExclude
  );

  /**
   * Creates a general point router while excluding the given set of points and paths from it.
   * <p>
   * In contrast to point routers that are created via
   * {@link #createPointRouter(Vehicle, Set, Set)}, a general point router is not affected by any
   * path properties or any configured edge evaluators. This means, for example, that a general
   * point router <em>always</em> considers paths regardless of whether the path is locked or not.
   * (Unless, of course, it is contained in the set of paths to be excluded.)
   * </p>
   *
   * @param pointsToExclude The set of points to be excluded.
   * @param pathsToExclude The set of paths to be excluded.
   * @return The point router.
   */
  PointRouter createGeneralPointRouter(
      Set<Point> pointsToExclude,
      Set<Path> pathsToExclude
  );
}
