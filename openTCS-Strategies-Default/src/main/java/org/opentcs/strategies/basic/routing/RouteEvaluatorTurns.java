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
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import static org.opentcs.data.ObjectPropConstants.PATH_TRAVEL_ORIENTATION;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;

/**
 * Computes costs for routes based, adding penalties for every change of the vehicle's orientation
 * on the route.
 * This cost function can be used to compute routes with a minimal number of changes of the
 * vehicle's orientation, which may be desirable when the additional time that such orientation
 * changes usually take must be taken into account.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RouteEvaluatorTurns
    implements RouteEvaluator {

  /**
   * The panelty.
   */
  private final long penalty;

  /**
   * Creates a new instance.
   *
   * @param penalty The penalty for course changes.
   */
  public RouteEvaluatorTurns(long penalty) {
    this.penalty = penalty;
  }

  @Override
  public long computeCosts(Vehicle vehicle, Point startPoint, List<Route.Step> steps) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(startPoint, "startPoint");
    requireNonNull(steps, "steps");

    long result = 0;
    Route.Step previousStep = null;
    for (Route.Step step : steps) {
      if (previousStep != null
          && !sameOrientation(previousStep.getPath(), step.getPath())) {
        result += penalty;
      }
      previousStep = step;
    }
    return result;
  }

  private boolean sameOrientation(Path path1, Path path2) {
    return Objects.equals(path1.getProperties().get(PATH_TRAVEL_ORIENTATION),
                          path2.getProperties().get(PATH_TRAVEL_ORIENTATION));
  }
}
