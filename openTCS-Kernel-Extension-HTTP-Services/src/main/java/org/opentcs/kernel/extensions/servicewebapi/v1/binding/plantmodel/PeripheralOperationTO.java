/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.peripherals.PeripheralOperation;

/**
 */
public class PeripheralOperationTO {

  private String operation;
  private String locationName;
  private String executionTrigger = PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION.name();
  private boolean completionRequired;

  @JsonCreator
  public PeripheralOperationTO(
      @Nonnull @JsonProperty(value = "operation", required = true) String operation,
      @Nonnull @JsonProperty(value = "locationName", required = true) String locationName
  ) {
    this.operation = requireNonNull(operation, "operation");
    this.locationName = requireNonNull(locationName, "locationName");
  }

  @Nonnull
  public String getOperation() {
    return operation;
  }

  public PeripheralOperationTO setOperation(@Nonnull String operation) {
    this.operation = requireNonNull(operation, "operation");
    return this;
  }

  @Nonnull
  public String getLocationName() {
    return locationName;
  }

  public PeripheralOperationTO setLocationName(@Nonnull String locationName) {
    this.locationName = requireNonNull(locationName, "locationName");
    return this;
  }

  @Nonnull
  public String getExecutionTrigger() {
    return executionTrigger;
  }

  public PeripheralOperationTO setExecutionTrigger(@Nonnull String executionTrigger) {
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

}
