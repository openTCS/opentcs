// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v6;

import static java.util.Objects.requireNonNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public class PeripheralOperationTO
    extends
      PlantModelElementTO {

  private String locationName = "";
  private ExecutionTrigger executionTrigger = ExecutionTrigger.AFTER_ALLOCATION;
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
    AFTER_MOVEMENT;
    // CHECKSTYLE:ON
  }

}
