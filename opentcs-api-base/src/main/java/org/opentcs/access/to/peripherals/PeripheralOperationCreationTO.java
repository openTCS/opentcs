// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.to.peripherals;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.util.Map;
import org.opentcs.access.to.CreationTO;

/**
 * A transfer object describing an operation to be performed by a peripheral device.
 */
public class PeripheralOperationCreationTO
    extends
      CreationTO
    implements
      Serializable {

  /**
   * The operation to be performed by the peripheral device.
   */
  private final String operation;
  /**
   * The name of the location the peripheral device is associated with.
   */
  @Nonnull
  private final String locationName;
  /**
   * The moment at which this operation is to be performed.
   */
  @Nonnull
  private final ExecutionTrigger executionTrigger;
  /**
   * Whether the completion of this operation is required to allow a vehicle to continue driving.
   */
  private final boolean completionRequired;

  /**
   * Creates a new instance with {@code executionTrigger} set to
   * {@link ExecutionTrigger#IMMEDIATE} and {@code completionRequired}
   * set to {@code false}.
   *
   * @param operation The operation to be performed by the peripheral device.
   * @param locationName The name of the location the peripheral device is associated with.
   */
  public PeripheralOperationCreationTO(
      @Nonnull
      String operation,
      @Nonnull
      String locationName
  ) {
    super("");
    this.operation = requireNonNull(operation, "operation");
    this.locationName = requireNonNull(locationName, "locationName");
    this.executionTrigger = ExecutionTrigger.IMMEDIATE;
    this.completionRequired = false;
  }

  private PeripheralOperationCreationTO(
      @Nonnull
      String name,
      @Nonnull
      Map<String, String> properties,
      @Nonnull
      String operation,
      @Nonnull
      String locationName,
      @Nonnull
      ExecutionTrigger executionTrigger,
      boolean completionRequired
  ) {
    super(name, properties);
    this.operation = requireNonNull(operation, "operation");
    this.locationName = requireNonNull(locationName, "locationName");
    this.executionTrigger = requireNonNull(executionTrigger, "executionTrigger");
    this.completionRequired = completionRequired;
  }

  @Override
  public PeripheralOperationCreationTO withName(
      @Nonnull
      String name
  ) {
    return new PeripheralOperationCreationTO(
        name,
        getModifiableProperties(),
        operation,
        locationName,
        executionTrigger,
        completionRequired
    );
  }

  @Override
  public PeripheralOperationCreationTO withProperties(
      @Nonnull
      Map<String, String> properties
  ) {
    return new PeripheralOperationCreationTO(
        getName(),
        properties,
        operation,
        locationName,
        executionTrigger,
        completionRequired
    );
  }

  @Override
  public PeripheralOperationCreationTO withProperty(
      @Nonnull
      String key,
      @Nonnull
      String value
  ) {
    return new PeripheralOperationCreationTO(
        getName(),
        propertiesWith(key, value),
        operation,
        locationName,
        executionTrigger,
        completionRequired
    );
  }

  /**
   * Returns the operation to be performed by the peripheral device.
   *
   * @return The operation to be performed by the peripheral device.
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
  public PeripheralOperationCreationTO withOperation(
      @Nonnull
      String operation
  ) {
    return new PeripheralOperationCreationTO(
        getName(),
        getModifiableProperties(),
        operation,
        locationName,
        executionTrigger,
        completionRequired
    );
  }

  /**
   * Returns the name of the location the peripheral device is associated with.
   *
   * @return The name of the location the peripheral device is associated with.
   */
  @Nonnull
  public String getLocationName() {
    return locationName;
  }

  /**
   * Creates a copy of this object, with the given location name.
   *
   * @param locationName The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralOperationCreationTO withLocationName(
      @Nonnull
      String locationName
  ) {
    return new PeripheralOperationCreationTO(
        getName(),
        getModifiableProperties(),
        operation,
        locationName,
        executionTrigger,
        completionRequired
    );
  }

  /**
   * Returns the moment at which this operation is to be performed.
   *
   * @return The moment at which this operation is to be performed.
   */
  @Nonnull
  public ExecutionTrigger getExecutionTrigger() {
    return executionTrigger;
  }

  /**
   * Creates a copy of this object, with the given execution trigger.
   * <p>
   * This method should only be used by the vehicle controller component of the baseline project.
   * </p>
   *
   * @param executionTrigger The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralOperationCreationTO withExecutionTrigger(
      @Nonnull
      ExecutionTrigger executionTrigger
  ) {
    return new PeripheralOperationCreationTO(
        getName(),
        getModifiableProperties(),
        operation,
        locationName,
        executionTrigger,
        completionRequired
    );
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
   * Creates a copy of this object, with the given completion required flag.
   * <p>
   * This method should only be used by the vehicle controller component of the baseline project.
   * </p>
   *
   * @param completionRequired The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralOperationCreationTO withCompletionRequired(boolean completionRequired) {
    return new PeripheralOperationCreationTO(
        getName(),
        getModifiableProperties(),
        operation,
        locationName,
        executionTrigger,
        completionRequired
    );
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
     * The operation is to be triggered after the allocation of the path / before the movement.
     */
    AFTER_ALLOCATION,
    /**
     * The operation is to be triggered after the movement.
     */
    AFTER_MOVEMENT;
  }
}
