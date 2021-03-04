/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import org.opentcs.data.model.Vehicle;

/**
 * Implementations of this interface construct point routers.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface PointRouterFactory {

  /**
   * Creates a point router for the given vehicle.
   *
   * @param vehicle The vehicle.
   * @return The point router.
   */
  PointRouter createPointRouter(Vehicle vehicle);
}
