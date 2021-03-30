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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    implements MovementCommand {

  /**
   * The route that this movement belongs to.
   */
  private final Route route;
  /**
   * The step describing the movement.
   */
  private final Route.Step step;
  /**
   * The operation to be executed after moving.
   */
  private final String operation;
  /**
   * The location at which the operation is to be executed. (May be
   * <code>null</code> if <em>operation</em> is <code>NO_OPERATION</code>.)
   */
  private final Location opLocation;
  /**
   * Indicates whether this movement is the final one for the drive order it belongs to.
   */
  private final boolean finalMovement;
  /**
   * The destination position of the whole drive order.
   */
  private final Point finalDestination;
  /**
   * The destination location of the whole drive order.
   */
  private final Location finalDestinationLocation;
  /**
   * The operation to be executed at the destination position.
   */
  private final String finalOperation;
  /**
   * Properties of the order this command is part of.
   */
  private final Map<String, String> properties;

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
    this.step = requireNonNull(step, "step");
    this.operation = requireNonNull(operation, "operation");
    this.finalMovement = finalMovement;
    this.finalDestinationLocation = finalDestinationLocation;
    this.finalDestination = requireNonNull(finalDestination, "finalDestination");
    this.finalOperation = requireNonNull(finalOperation, "finalOperation");
    this.properties = requireNonNull(properties, "properties");
    if (opLocation == null && !isEmptyOperation(operation)) {
      throw new NullPointerException("opLocation");
    }
    this.opLocation = opLocation;

    this.route = requireNonNull(route, "route");
  }

  @Override
  public Route getRoute() {
    return route;
  }

  @Nonnull
  @Override
  public Route.Step getStep() {
    return step;
  }

  @Nonnull
  @Override
  public String getOperation() {
    return operation;
  }

  @Override
  public boolean isWithoutOperation() {
    return isEmptyOperation(operation);
  }

  @Nullable
  @Override
  public Location getOpLocation() {
    return opLocation;
  }

  @Override
  public boolean isFinalMovement() {
    return finalMovement;
  }

  @Nonnull
  @Override
  public Point getFinalDestination() {
    return finalDestination;
  }

  @Nullable
  @Override
  public Location getFinalDestinationLocation() {
    return finalDestinationLocation;
  }

  @Nonnull
  @Override
  public String getFinalOperation() {
    return finalOperation;
  }

  @Nonnull
  @Override
  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof MovementCommand) {
      MovementCommand other = (MovementCommand) o;
      return step.equals(other.getStep()) && operation.equals(other.getOperation());
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return step.hashCode() ^ operation.hashCode();
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

  /**
   * Checks whether an operation means something is to be done in addition to
   * moving or not.
   *
   * @param operation The operation to be checked.
   * @return <code>true</code> if, and only if, the vehicle should only move
   * with the given operation.
   */
  private static boolean isEmptyOperation(String operation) {
    return NO_OPERATION.equals(operation)
        || MOVE_OPERATION.equals(operation)
        || PARK_OPERATION.equals(operation);
  }
}
