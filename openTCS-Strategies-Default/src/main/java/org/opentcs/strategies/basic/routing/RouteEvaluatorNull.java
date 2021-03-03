/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import java.util.List;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;

/**
 * Always returns 0, for every given route.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RouteEvaluatorNull
    implements RouteEvaluator {

  /**
   * Creates a new instance.
   */
  public RouteEvaluatorNull() {
  }

  @Override
  public long computeCosts(Vehicle vehicle, Point startPoint, List<Route.Step> steps) {
    return 0;
  }
}
