/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared;

import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.data.peripherals.PeripheralOperation.ExecutionTrigger;

/**
 * Describes a peripheral operation.
 */
public class PeripheralOperationDescription {

  private String operation;

  private String locationName;

  private ExecutionTrigger executionTrigger;

  private boolean completionRequired;

  public PeripheralOperationDescription() {
  }

  public String getOperation() {
    return operation;
  }

  public PeripheralOperationDescription setOperation(String operation) {
    this.operation = operation;
    return this;
  }

  public String getLocationName() {
    return locationName;
  }

  public PeripheralOperationDescription setLocationName(String locationName) {
    this.locationName = locationName;
    return this;
  }

  public ExecutionTrigger getExecutionTrigger() {
    return executionTrigger;
  }

  public PeripheralOperationDescription setExecutionTrigger(ExecutionTrigger executionTrigger) {
    this.executionTrigger = executionTrigger;
    return this;
  }

  public boolean isCompletionRequired() {
    return completionRequired;
  }

  public PeripheralOperationDescription setCompletionRequired(boolean completionRequired) {
    this.completionRequired = completionRequired;
    return this;
  }

  public static PeripheralOperationDescription fromPeripheralOperation(
      PeripheralOperation operation) {
    PeripheralOperationDescription state = new PeripheralOperationDescription();
    state.operation = operation.getOperation();
    state.locationName = operation.getLocation().getName();
    state.executionTrigger = operation.getExecutionTrigger();
    state.completionRequired = operation.isCompletionRequired();
    return state;
  }

}
