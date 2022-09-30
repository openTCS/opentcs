/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.status.binding;

import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.data.peripherals.PeripheralOperation.ExecutionTrigger;

/**
 * Describes a peripheral operation.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
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

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public String getLocationName() {
    return locationName;
  }

  public void setLocationName(String locationName) {
    this.locationName = locationName;
  }

  public ExecutionTrigger getExecutionTrigger() {
    return executionTrigger;
  }

  public void setExecutionTrigger(ExecutionTrigger executionTrigger) {
    this.executionTrigger = executionTrigger;
  }

  public boolean isCompletionRequired() {
    return completionRequired;
  }

  public void setCompletionRequired(boolean completionRequired) {
    this.completionRequired = completionRequired;
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
