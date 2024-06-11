/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;

/**
 * A command for moving a step.
 */
public class MovementCommand {

  /**
   * A constant indicating there is no operation to be executed after moving.
   */
  public static final String NO_OPERATION = DriveOrder.Destination.OP_NOP;
  /**
   * A constant indicating the vehicle should basically just move to a point without a location
   * associated to it.
   */
  public static final String MOVE_OPERATION = DriveOrder.Destination.OP_MOVE;
  /**
   * A constant for parking the vehicle. (Again, basically doing nothing at the destination.)
   */
  public static final String PARK_OPERATION = DriveOrder.Destination.OP_PARK;
  /**
   * The transport order this movement belongs to.
   */
  private final TransportOrder transportOrder;
  /**
   * The drive order this movement belongs to.
   */
  private final DriveOrder driveOrder;
  /**
   * The step describing the movement.
   */
  private final Route.Step step;
  /**
   * The operation to be executed after moving.
   */
  private final String operation;
  /**
   * The location at which the operation is to be executed.
   * May be <code>null</code> if the movement command's <em>operation</em> is considred an empty
   * operation (i.e. is {@link #NO_OPERATION}, {@link #MOVE_OPERATION} or {@link #PARK_OPERATION}).
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
   * @param transportOrder The transport order this movement belongs to.
   * @param driveOrder The drive order this movement belongs to.
   * @param step The step describing the movement.
   * @param operation The operation to be executed after moving.
   * @param opLocation The location at which the operation is to be executed.
   * May be <code>null</code> if the movement command's <em>operation</em> is considred an empty
   * operation (i.e. is {@link #NO_OPERATION}, {@link #MOVE_OPERATION} or {@link #PARK_OPERATION}).
   * @param finalMovement Indicates whether this movement is the final one in the drive order it
   * belongs to.
   * @param finalDestinationLocation The destination location of the whole drive order.
   * @param finalDestination The destination position of the whole drive order.
   * @param finalOperation The operation to be executed at the destination position.
   * @param properties Properties of the order this command is part of.
   */
  public MovementCommand(
      @Nonnull
      TransportOrder transportOrder,
      @Nonnull
      DriveOrder driveOrder,
      @Nonnull
      Route.Step step,
      @Nonnull
      String operation,
      @Nullable
      Location opLocation,
      boolean finalMovement,
      @Nullable
      Location finalDestinationLocation,
      @Nonnull
      Point finalDestination,
      @Nonnull
      String finalOperation,
      @Nonnull
      Map<String, String> properties
  ) {
    this.transportOrder = requireNonNull(transportOrder, "transportOrder");
    this.driveOrder = requireNonNull(driveOrder, "driveOrder");
    this.step = requireNonNull(step, "step");
    this.operation = requireNonNull(operation, "operation");
    this.finalMovement = finalMovement;
    this.finalDestinationLocation = finalDestinationLocation;
    this.finalDestination = requireNonNull(finalDestination, "finalDestination");
    this.finalOperation = requireNonNull(finalOperation, "finalOperation");
    this.properties = requireNonNull(properties, "properties");
    if (opLocation == null && !isEmptyOperation(operation)) {
      throw new NullPointerException("opLocation is null while operation is not considered empty");
    }
    this.opLocation = opLocation;
  }

  /**
   * Returns the transport order this movement belongs to.
   *
   * @return The transport order this movement belongs to.
   */
  @Nonnull
  public TransportOrder getTransportOrder() {
    return transportOrder;
  }

  /**
   * Creates a copy of this object, with the given transport order.
   *
   * @param transportOrder The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public MovementCommand withTransportOrder(
      @Nonnull
      TransportOrder transportOrder
  ) {
    return new MovementCommand(
        transportOrder,
        driveOrder,
        step,
        operation,
        opLocation,
        finalMovement,
        finalDestinationLocation,
        finalDestination,
        finalOperation,
        properties
    );
  }

  /**
   * Returns the drive order this movement belongs to.
   *
   * @return The drive order this movement belongs to.
   */
  @Nonnull
  public DriveOrder getDriveOrder() {
    return driveOrder;
  }

  /**
   * Creates a copy of this object, with the given drive order.
   *
   * @param driveOrder The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public MovementCommand withDriveOrder(
      @Nonnull
      DriveOrder driveOrder
  ) {
    return new MovementCommand(
        transportOrder,
        driveOrder,
        step,
        operation,
        opLocation,
        finalMovement,
        finalDestinationLocation,
        finalDestination,
        finalOperation,
        properties
    );
  }

  /**
   * Returns the step describing the movement.
   *
   * @return The step describing the movement.
   */
  @Nonnull
  public Route.Step getStep() {
    return step;
  }

  /**
   * Creates a copy of this object, with the given step.
   *
   * @param step The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public MovementCommand withStep(
      @Nonnull
      Route.Step step
  ) {
    return new MovementCommand(
        transportOrder,
        driveOrder,
        step,
        operation,
        opLocation,
        finalMovement,
        finalDestinationLocation,
        finalDestination,
        finalOperation,
        properties
    );
  }

  /**
   * Returns the operation to be executed after moving.
   *
   * @return The operation to be executed after moving.
   */
  @Nonnull
  public String getOperation() {
    return operation;
  }

  /**
   * Creates a copy of this object, with the given operation.
   *
   * @param operation The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public MovementCommand withOperation(
      @Nonnull
      String operation
  ) {
    return new MovementCommand(
        transportOrder,
        driveOrder,
        step,
        operation,
        opLocation,
        finalMovement,
        finalDestinationLocation,
        finalDestination,
        finalOperation,
        properties
    );
  }

  /**
   * Indicates whether an operation is to be executed in addition to moving or not.
   *
   * @return <code>true</code> if, and only if, no operation is to be executed.
   */
  public boolean hasEmptyOperation() {
    return isEmptyOperation(operation);
  }

  /**
   * Returns the location at which the operation is to be executed.
   * <p>
   * May be <code>null</code> if the movement command's <em>operation</em> is considred an empty
   * operation (i.e. is {@link #NO_OPERATION}, {@link #MOVE_OPERATION} or {@link #PARK_OPERATION}).
   * </p>
   *
   * @return The location at which the operation is to be executed.
   */
  @Nullable
  public Location getOpLocation() {
    return opLocation;
  }

  /**
   * Creates a copy of this object, with the given location at which the operation is to be
   * executed.
   * <p>
   * May be <code>null</code> if the movement command's <em>operation</em> is considred an empty
   * operation (i.e. is {@link #NO_OPERATION}, {@link #MOVE_OPERATION} or {@link #PARK_OPERATION}).
   * </p>
   *
   * @param opLocation The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public MovementCommand withOpLocation(
      @Nullable
      Location opLocation
  ) {
    return new MovementCommand(
        transportOrder,
        driveOrder,
        step,
        operation,
        opLocation,
        finalMovement,
        finalDestinationLocation,
        finalDestination,
        finalOperation,
        properties
    );
  }

  /**
   * Indicates whether this movement is the final one in the driver order it belongs to.
   *
   * @return <code>true</code> if, and only if, this movement is the final one.
   */
  public boolean isFinalMovement() {
    return finalMovement;
  }

  /**
   * Creates a copy of this object, with the given final movement flag.
   *
   * @param finalMovement The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public MovementCommand withFinalMovement(boolean finalMovement) {
    return new MovementCommand(
        transportOrder,
        driveOrder,
        step,
        operation,
        opLocation,
        finalMovement,
        finalDestinationLocation,
        finalDestination,
        finalOperation,
        properties
    );
  }

  /**
   * Returns the final destination of the drive order this MovementCommand was created for.
   *
   * @return The final destination of the drive order this MovementCommand was created for.
   */
  @Nonnull
  public Point getFinalDestination() {
    return finalDestination;
  }

  /**
   * Creates a copy of this object, with the given final destination.
   *
   * @param finalDestination The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public MovementCommand withFinalDestination(
      @Nonnull
      Point finalDestination
  ) {
    return new MovementCommand(
        transportOrder,
        driveOrder,
        step,
        operation,
        opLocation,
        finalMovement,
        finalDestinationLocation,
        finalDestination,
        finalOperation,
        properties
    );
  }

  /**
   * Returns the destination location of the whole drive order.
   *
   * @return The destination location of the whole drive order.
   */
  @Nullable
  public Location getFinalDestinationLocation() {
    return finalDestinationLocation;
  }

  /**
   * Creates a copy of this object, with the given final destination location.
   *
   * @param finalDestinationLocation The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public MovementCommand withFinalDestinationLocation(
      @Nullable
      Location finalDestinationLocation
  ) {
    return new MovementCommand(
        transportOrder,
        driveOrder,
        step,
        operation,
        opLocation,
        finalMovement,
        finalDestinationLocation,
        finalDestination,
        finalOperation,
        properties
    );
  }

  /**
   * Returns the operation to be executed at the <em>final</em> destination position.
   *
   * @return The operation to be executed at the <em>final</em> destination position.
   */
  @Nonnull
  public String getFinalOperation() {
    return finalOperation;
  }

  /**
   * Creates a copy of this object, with the given final operation.
   *
   * @param finalOperation The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public MovementCommand withFinalOperation(
      @Nonnull
      String finalOperation
  ) {
    return new MovementCommand(
        transportOrder,
        driveOrder,
        step,
        operation,
        opLocation,
        finalMovement,
        finalDestinationLocation,
        finalDestination,
        finalOperation,
        properties
    );
  }

  /**
   * Returns the properties of the order this command is part of.
   *
   * @return The properties of the order this command is part of.
   */
  @Nonnull
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * Creates a copy of this object, with the given properties.
   *
   * @param properties The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public MovementCommand withProperties(
      @Nonnull
      Map<String, String> properties
  ) {
    return new MovementCommand(
        transportOrder,
        driveOrder,
        step,
        operation,
        opLocation,
        finalMovement,
        finalDestinationLocation,
        finalDestination,
        finalOperation,
        properties
    );
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MovementCommand)) {
      return false;
    }

    MovementCommand other = (MovementCommand) o;
    return step.equals(other.getStep()) && operation.equals(other.getOperation());
  }

  /**
   * Compares the given movement command to this movement command, ignoring rerouting-related
   * properties.
   *
   * @param command The movement command to compare to.
   * @return {@code true}, if the given movement command is equal to this movement command
   * (ignoring rerouting-related properties), otherwise {@code false}.
   */
  public boolean equalsInMovement(MovementCommand command) {
    if (command == null) {
      return false;
    }

    return this.getStep().equalsInMovement(command.getStep())
        && Objects.equals(this.getOperation(), command.getOperation());
  }

  @Override
  public int hashCode() {
    return step.hashCode() ^ operation.hashCode();
  }

  @Override
  public String toString() {
    return "MovementCommand{"
        + "transportOrder=" + getTransportOrder()
        + ", driveOrder=" + getDriveOrder()
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
   * Checks whether an operation means something is to be done in addition to moving or not.
   *
   * @param operation The operation to be checked.
   * @return <code>true</code> if, and only if, the vehicle should only move with the given
   * operation.
   */
  private boolean isEmptyOperation(String operation) {
    return NO_OPERATION.equals(operation)
        || MOVE_OPERATION.equals(operation)
        || PARK_OPERATION.equals(operation);
  }
}
