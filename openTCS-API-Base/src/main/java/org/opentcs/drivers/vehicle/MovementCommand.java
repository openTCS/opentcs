/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.Route.Step;

/**
 * A command for moving a step.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface MovementCommand {

  /**
   * A constant indicating there is no operation to be executed after moving.
   */
  String NO_OPERATION = DriveOrder.Destination.OP_NOP;
  /**
   * A constant indicating the vehicle should basically just move to a point
   * without a location associated to it.
   */
  String MOVE_OPERATION = DriveOrder.Destination.OP_MOVE;
  /**
   * A constant for parking the vehicle. (Again, basically doing nothing at the destination.)
   */
  String PARK_OPERATION = DriveOrder.Destination.OP_PARK;

  /**
   * Returns the route that this movement belongs to.
   *
   * @return The route that this movement belongs to.
   */
  @Nonnull
  Route getRoute();

  /**
   * Returns the step describing the movement.
   *
   * @return The step describing the movement.
   */
  @Nonnull
  Step getStep();

  /**
   * Returns the operation to be executed after moving.
   *
   * @return The operation to be executed after moving.
   */
  @Nonnull
  String getOperation();

  /**
   * Checks whether an operation is to be executed in addition to moving or not.
   *
   * @return <code>true</code> if, and only if, no operation is to be executed.
   */
  boolean isWithoutOperation();

  /**
   * Returns the location at which the operation is to be executed. (May be
   * <code>null</code> if <em>operation</em> is <code>NO_OPERATION</code>.)
   *
   * @return The location at which the operation is to be executed.
   */
  @Nullable
  Location getOpLocation();

  /**
   * Indicates whether this movement is the final one in the driver order it belongs to.
   *
   * @return <code>true</code> if, and only if, this movement is the final one.
   */
  boolean isFinalMovement();

  /**
   * Returns the final destination of the drive order this MovementCommand was
   * created for.
   *
   * @return The final destination of the drive order this MovementCommand was
   * created for.
   */
  @Nonnull
  Point getFinalDestination();

  /**
   * Returns the destination location of the whole drive order.
   *
   * @return The destination location of the whole drive order.
   */
  @Nullable
  Location getFinalDestinationLocation();

  /**
   * Returns the operation to be executed at the <em>final</em> destination
   * position.
   *
   * @return The operation to be executed at the <em>final</em> destination
   * position.
   */
  @Nonnull
  String getFinalOperation();

  /**
   * Returns the properties of the order this command is part of.
   *
   * @return The properties of the order this command is part of.
   */
  @Nonnull
  Map<String, String> getProperties();
}
