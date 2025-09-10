// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;

/**
 */
public class PeripheralOperationTO {

  private String operation;
  private String locationName;
  private ExecutionTrigger executionTrigger = ExecutionTrigger.AFTER_ALLOCATION;
  private boolean completionRequired;

  @JsonCreator
  public PeripheralOperationTO(
      @Nonnull
      @JsonProperty(value = "operation", required = true)
      String operation,
      @Nonnull
      @JsonProperty(value = "locationName", required = true)
      String locationName
  ) {
    this.operation = requireNonNull(operation, "operation");
    this.locationName = requireNonNull(locationName, "locationName");
  }

  @Nonnull
  public String getOperation() {
    return operation;
  }

  public PeripheralOperationTO setOperation(
      @Nonnull
      String operation
  ) {
    this.operation = requireNonNull(operation, "operation");
    return this;
  }

  @Nonnull
  public String getLocationName() {
    return locationName;
  }

  public PeripheralOperationTO setLocationName(
      @Nonnull
      String locationName
  ) {
    this.locationName = requireNonNull(locationName, "locationName");
    return this;
  }

  @Nonnull
  public ExecutionTrigger getExecutionTrigger() {
    return executionTrigger;
  }

  public PeripheralOperationTO setExecutionTrigger(
      @Nonnull
      ExecutionTrigger executionTrigger
  ) {
    this.executionTrigger = requireNonNull(executionTrigger, "executionTrigger");
    return this;
  }

  public boolean isCompletionRequired() {
    return completionRequired;
  }

  public PeripheralOperationTO setCompletionRequired(boolean completionRequired) {
    this.completionRequired = completionRequired;
    return this;
  }

  // CHECKSTYLE:OFF
  public enum ExecutionTrigger {

    IMMEDIATE,
    AFTER_ALLOCATION,
    AFTER_MOVEMENT;
  }
  // CHECKSTYLE:ON
}
