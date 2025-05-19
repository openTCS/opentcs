// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared;

import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PeripheralOperationTO;

/**
 * Describes a peripheral operation.
 */
public class PeripheralOperationDescription {

  private String operation;

  private String locationName;

  private PeripheralOperationTO.ExecutionTrigger executionTrigger;

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

  public PeripheralOperationTO.ExecutionTrigger getExecutionTrigger() {
    return executionTrigger;
  }

  public PeripheralOperationDescription setExecutionTrigger(
      PeripheralOperationTO.ExecutionTrigger executionTrigger
  ) {
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
}
