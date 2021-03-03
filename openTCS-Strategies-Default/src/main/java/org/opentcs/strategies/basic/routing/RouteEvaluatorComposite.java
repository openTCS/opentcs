/*
 * openTCS copyright information:
 * Copyright (c) 2017 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;

/**
 * A {@link RouteEvaluator} computing costs as the sum of the costs computed by its components.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RouteEvaluatorComposite
    implements RouteEvaluator {

  /**
   * The components.
   */
  private final Set<RouteEvaluator> components = new HashSet<>();

  /**
   * Creates a new instance.
   */
  public RouteEvaluatorComposite() {
  }

  @Override
  public long computeCosts(Vehicle vehicle, Point startPoint, List<Route.Step> steps) {
    long result = 0;
    for (RouteEvaluator component : components) {
      result += component.computeCosts(vehicle, startPoint, steps);
    }
    return result;
  }

  /**
   * Returns the {@link RouteEvaluator}s that make up this composite.
   *
   * @return The components.
   */
  public Set<RouteEvaluator> getComponents() {
    return components;
  }
}
