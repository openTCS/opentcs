// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v004;

import static java.util.Objects.requireNonNull;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public class PeripheralOperationTO
    extends
      PlantModelElementTO {

  private String locationName = "";
  private ExecutionTrigger executionTrigger = ExecutionTrigger.BEFORE_MOVEMENT;
  private boolean completionRequired;

  /**
   * Creates a new instance.
   */
  public PeripheralOperationTO() {
  }

  @XmlAttribute(required = true)
  public String getLocationName() {
    return locationName;
  }

  public PeripheralOperationTO setLocationName(String locationName) {
    this.locationName = requireNonNull(locationName, "locationName");
    return this;
  }

  @XmlAttribute(required = true)
  public ExecutionTrigger getExecutionTrigger() {
    return executionTrigger;
  }

  public PeripheralOperationTO setExecutionTrigger(ExecutionTrigger executionTrigger) {
    this.executionTrigger = requireNonNull(executionTrigger, "executionTrigger");
    return this;
  }

  @XmlAttribute(required = true)
  public boolean isCompletionRequired() {
    return completionRequired;
  }

  public PeripheralOperationTO setCompletionRequired(boolean completionRequired) {
    this.completionRequired = completionRequired;
    return this;
  }

  @XmlType
  public enum ExecutionTrigger {
    // CHECKSTYLE:OFF
    AFTER_ALLOCATION,
    BEFORE_MOVEMENT,
    AFTER_MOVEMENT;
    // CHECKSTYLE:ON
  }

}
