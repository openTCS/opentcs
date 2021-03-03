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
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import static org.opentcs.strategies.basic.routing.RoutingTable.INFINITE_COSTS;

/**
 * Computes routing costs based on the time a vehicle needs to travel along the
 * routes' paths.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RouteEvaluatorTravelTime
    implements RouteEvaluator {

  /**
   * Creates a new instance.
   */
  public RouteEvaluatorTravelTime() {
  }

  @Override
  public long computeCosts(Vehicle vehicle, Point startPoint, List<Route.Step> steps) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(startPoint, "startPoint");
    requireNonNull(steps, "steps");

    long result = 0;
    for (Route.Step step : steps) {
      long travelTime
          = travelTime(vehicle, step.getPath(), step.getVehicleOrientation());
      if (travelTime == INFINITE_COSTS) {
        return INFINITE_COSTS;
      }
      result += travelTime;
    }
    return result;
  }

  private long travelTime(Vehicle vehicle, Path path, Vehicle.Orientation orientation) {
    int maxVelocity;
    if (Objects.equals(Vehicle.Orientation.BACKWARD, orientation)) {
      maxVelocity = Math.min(vehicle.getMaxReverseVelocity(),
                             path.getMaxReverseVelocity());
    }
    else {
      maxVelocity = Math.min(vehicle.getMaxVelocity(), path.getMaxVelocity());
    }
    return (maxVelocity == 0) ? INFINITE_COSTS : path.getLength() / maxVelocity;
  }
}
