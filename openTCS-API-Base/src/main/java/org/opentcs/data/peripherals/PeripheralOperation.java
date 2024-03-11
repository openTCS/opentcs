/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.peripherals;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Describes an operation that is to be executed by a peripheral device.
 */
public class PeripheralOperation
    implements Serializable {

  /**
   * The location the peripheral device is associated with.
   */
  @Nonnull
  private final TCSResourceReference<Location> location;
  /**
   * The actual operation to be executed by the peripheral device.
   */
  @Nonnull
  private final String operation;
  /**
   * The moment at which this operation is to be executed.
   */
  @Nonnull
  private final ExecutionTrigger executionTrigger;
  /**
   * Whether the completion of this operation is required to allow a vehicle to continue driving.
   */
  private final boolean completionRequired;

  /**
   * Creates a new instance.
   *
   * @param location The location the peripheral device is associated with.
   * @param operation The actual operation to be executed by the peripheral device.
   * @param executionTrigger The moment at which this operation is to be executed.
   * @param completionRequired Whether the completion of this operation is required to allow a
   * vehicle to continue driving.
   */
  public PeripheralOperation(@Nonnull TCSResourceReference<Location> location,
                             @Nonnull String operation,
                             @Nonnull ExecutionTrigger executionTrigger,
                             boolean completionRequired) {
    this.location = requireNonNull(location, "location");
    this.operation = requireNonNull(operation, "operation");
    requireNonNull(executionTrigger, "executionTrigger");
    this.executionTrigger = (executionTrigger == ExecutionTrigger.BEFORE_MOVEMENT)
        ? ExecutionTrigger.AFTER_ALLOCATION
        : executionTrigger;
    this.completionRequired = completionRequired;
  }

  /**
   * Returns the location the peripheral device is associated with.
   *
   * @return The location the peripheral device is associated with.
   */
  @Nonnull
  public TCSResourceReference<Location> getLocation() {
    return location;
  }

  /**
   * Returns the actual operation to be executed by the peripheral device.
   *
   * @return The actual operation to be executed by the peripheral device.
   */
  @Nonnull
  public String getOperation() {
    return operation;
  }

  /**
   * Returns the moment at which this operation is to be executed.
   *
   * @return The moment at which this operation is to be executed.
   */
  @Nonnull
  public ExecutionTrigger getExecutionTrigger() {
    return executionTrigger;
  }

  /**
   * Returns whether the completion of this operation is required to allow a vehicle to continue
   * driving.
   *
   * @return Whether the completion of this operation is required to allow a vehicle to continue
   * driving.
   */
  public boolean isCompletionRequired() {
    return completionRequired;
  }

  /**
   * Defines the various moments at which an operation may be executed.
   */
  public enum ExecutionTrigger {
    /**
     * The operation is to be triggered immediately.
     */
    IMMEDIATE,
    /**
     * The operation is to be triggered before the movement.
     *
     * @deprecated Use {@link #AFTER_ALLOCATION} instead.
     */
    @Deprecated
    @ScheduledApiChange(when = "6.0", details = "Will be removed")
    BEFORE_MOVEMENT,
    /**
     * The operation is to be triggered after the allocation of the path / before the movement.
     */
    AFTER_ALLOCATION,
    /**
     * The operation is to be triggered after the movement.
     */
    AFTER_MOVEMENT;
  }

  @Override
  public String toString() {
    return "PeripheralOperation{"
        + "location=" + location + ", "
        + "operation=" + operation + ", "
        + "executionTrigger=" + executionTrigger + ", "
        + "completionRequired=" + completionRequired + '}';
  }
}
