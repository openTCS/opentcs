/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.model;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.peripherals.PeripheralOperation;

/**
 * A Bean to hold the Peripheral operation.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class PeripheralOperationModel {

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

  public PeripheralOperationModel(String locationName,
                                  String operation,
                                  PeripheralOperation.ExecutionTrigger executionTrigger,
                                  boolean completionRequired) {
    this.operation = requireNonNull(operation, "operation");
    this.locationName = requireNonNull(locationName, "locationName");
    this.executionTrigger = requireNonNull(executionTrigger, "executionTrigger");
    this.completionRequired = requireNonNull(completionRequired, "completionRequired");
  }

  public String getOperation() {
    return operation;
  }

  public String getLocationName() {
    return locationName;
  }

  public PeripheralOperation.ExecutionTrigger getExecutionTrigger() {
    return executionTrigger;
  }

  public boolean isCompletionRequired() {
    return completionRequired;
  }

}
