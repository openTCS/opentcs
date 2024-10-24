// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v004;

import static java.util.Objects.requireNonNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public class PeripheralOperationTO
    extends
      PlantModelElementTO {

  private String locationName = "";
  private String executionTrigger = "";
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
  public String getExecutionTrigger() {
    return executionTrigger;
  }

  public PeripheralOperationTO setExecutionTrigger(String executionTrigger) {
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

}
