/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import java.util.Map;
import static java.util.Objects.requireNonNull;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.Route;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * The default movement command implementation.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class MovementCommandImpl
    extends MovementCommand {

  /**
   * The route that this movement belongs to.
   */
  private final Route route;

  /**
   * Creates a new instance.
   *
   * @param route The route that this movement belongs to.
   * @param step The step describing the movement.
   * @param operation The operation to be executed after moving.
   * @param opLocation The location at which the operation is to be executed.
   * May be <code>null</code> if <em>newOperation</em> is <code>NO_OPERATION</code>.)
   * @param finalMovement Indicates whether this movement is the final one in the drive order it
   * belongs to.
   * @param finalDestinationLocation The destination location of the whole drive order.
   * @param finalDestination The destination position of the whole drive order.
   * @param finalOperation The operation to be executed at the destination
   * position.
   * @param properties Properties of the order this command is part of.
   */
  @SuppressWarnings("deprecation")
  public MovementCommandImpl(Route route,
                            Route.Step step,
                            String operation,
                            Location opLocation,
                            boolean finalMovement,
                            Location finalDestinationLocation,
                            Point finalDestination,
                            String finalOperation,
                            Map<String, String> properties) {
    super(step,
          operation,
          opLocation,
          finalMovement,
          finalDestinationLocation,
          finalDestination,
          finalOperation,
          properties);

    this.route = requireNonNull(route, "route");
  }

  @Override
  public Route getRoute() {
    return route;
  }

  @Override
  public String toString() {
    return "MovementCommandImpl{"
        + "route=" + getRoute()
        + ", step=" + getStep()
        + ", operation=" + getOperation()
        + ", opLocation=" + getOpLocation()
        + ", finalMovement=" + isFinalMovement()
        + ", finalDestination=" + getFinalDestination()
        + ", finalDestinationLocation=" + getFinalDestinationLocation()
        + ", finalOperation=" + getFinalOperation()
        + ", properties=" + getProperties()
        + '}';
  }

}
