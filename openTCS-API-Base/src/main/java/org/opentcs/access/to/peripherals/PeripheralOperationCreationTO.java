/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.peripherals;

import java.io.Serializable;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;
import org.opentcs.data.peripherals.PeripheralOperation;

/**
 * A transfer object describing an operation to be performed by a peripheral device.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralOperationCreationTO
    extends CreationTO
    implements Serializable {

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
  private final PeripheralOperation.ExecutionTrigger executionTrigger;
  /**
   * Whether the completion of this operation is required to allow a vehicle to continue driving.
   */
  private final boolean completionRequired;

  /**
   * Creates a new instance with {@code executionTrigger} set to
   * {@link PeripheralOperation.ExecutionTrigger#BEFORE_MOVEMENT} and {@code completionRequired} set
   * to {@code false}.
   *
   * @param operation The operation to be performed by the peripheral device.
   * @param locationName The name of the location the peripheral device is associated with.
   */
  public PeripheralOperationCreationTO(@Nonnull String operation, @Nonnull String locationName) {
    super("");
    this.operation = requireNonNull(operation, "operation");
    this.locationName = requireNonNull(locationName, "locationName");
    this.executionTrigger = PeripheralOperation.ExecutionTrigger.BEFORE_MOVEMENT;
    this.completionRequired = false;
  }

  private PeripheralOperationCreationTO(
      @Nonnull String name,
      @Nonnull Map<String, String> properties,
      @Nonnull String operation,
      @Nonnull String locationName,
      @Nonnull PeripheralOperation.ExecutionTrigger executionTrigger,
      boolean completionRequired) {
    super(name, properties);
    this.operation = requireNonNull(operation, "operation");
    this.locationName = requireNonNull(locationName, "locationName");
    this.executionTrigger = requireNonNull(executionTrigger, "executionTrigger");
    this.completionRequired = completionRequired;
  }

  @Override
  public PeripheralOperationCreationTO withName(@Nonnull String name) {
    return new PeripheralOperationCreationTO(name,
                                             getModifiableProperties(),
                                             operation,
                                             locationName,
                                             executionTrigger,
                                             completionRequired);
  }

  @Override
  public PeripheralOperationCreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new PeripheralOperationCreationTO(getName(),
                                             properties,
                                             operation,
                                             locationName,
                                             executionTrigger,
                                             completionRequired);
  }

  @Override
  public PeripheralOperationCreationTO withProperty(@Nonnull String key, @Nonnull String value) {
    return new PeripheralOperationCreationTO(getName(),
                                             propertiesWith(key, value),
                                             operation,
                                             locationName,
                                             executionTrigger,
                                             completionRequired);
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
  public PeripheralOperationCreationTO withOperation(@Nonnull String operation) {
    return new PeripheralOperationCreationTO(getName(),
                                             getModifiableProperties(),
                                             operation,
                                             locationName,
                                             executionTrigger,
                                             completionRequired);
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
  public PeripheralOperationCreationTO withLocationName(@Nonnull String locationName) {
    return new PeripheralOperationCreationTO(getName(),
                                             getModifiableProperties(),
                                             operation,
                                             locationName,
                                             executionTrigger,
                                             completionRequired);
  }

  /**
   * Returns the moment at which this operation is to be performed.
   *
   * @return The moment at which this operation is to be performed.
   */
  @Nonnull
  public PeripheralOperation.ExecutionTrigger getExecutionTrigger() {
    return executionTrigger;
  }

  /**
   * Creates a copy of this object, with the given execution trigger.
   *
   * @param executionTrigger The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralOperationCreationTO withExecutionTrigger(
      @Nonnull PeripheralOperation.ExecutionTrigger executionTrigger) {
    return new PeripheralOperationCreationTO(getName(),
                                             getModifiableProperties(),
                                             operation,
                                             locationName,
                                             executionTrigger,
                                             completionRequired);
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
   *
   * @param completionRequired The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralOperationCreationTO withCompletionRequired(boolean completionRequired) {
    return new PeripheralOperationCreationTO(getName(),
                                             getModifiableProperties(),
                                             operation,
                                             locationName,
                                             executionTrigger,
                                             completionRequired);
  }
}
