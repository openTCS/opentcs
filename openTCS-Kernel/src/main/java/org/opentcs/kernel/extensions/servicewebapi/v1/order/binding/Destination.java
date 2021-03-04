/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.order.binding;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.LinkedList;
import java.util.List;
import javax.validation.constraints.Size;

/**
 * A destination of a transport.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Destination {

  @JsonProperty(required = true)
  @JsonPropertyDescription("The name of the destination location")
  private String locationName = "";

  @JsonProperty(required = true)
  @JsonPropertyDescription("The destination operation")
  private String operation = "";

  @JsonPropertyDescription("The drive order's properties")
  @Size(min = 0)
  private List<Property> properties = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public Destination() {
  }

  /**
   * Returns the location name.
   *
   * @return The location name
   */
  public String getLocationName() {
    return locationName;
  }

  /**
   * Sets the location name.
   *
   * @param locationName The new name
   */
  public void setLocationName(String locationName) {
    this.locationName = locationName;
  }

  /**
   * Returns the operation.
   *
   * @return The operation
   */
  public String getOperation() {
    return operation;
  }

  /**
   * Sets the operation.
   *
   * @param operation The new operation
   */
  public void setOperation(String operation) {
    this.operation = operation;
  }

  /**
   * Returns the destination's properties.
   *
   * @return The destination's properties.
   */
  public List<Property> getProperties() {
    return properties;
  }

  /**
   * Sets the destination's properties.
   *
   * @param properties The new destination's properties.
   */
  public void setProperties(List<Property> properties) {
    this.properties = properties;
  }
}
