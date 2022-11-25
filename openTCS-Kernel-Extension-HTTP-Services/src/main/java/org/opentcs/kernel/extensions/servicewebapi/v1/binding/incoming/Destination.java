/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.incoming;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * A destination of a transport.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Destination {

  private String locationName;

  private String operation;

  private List<Property> properties;

  @JsonCreator
  public Destination(
      @Nonnull @JsonProperty(required = true, value = "locationName") String locationName,
      @Nonnull @JsonProperty(required = true, value = "operation") String operation,
      @Nullable @JsonProperty(required = false, value = "properties") List<Property> properties) {
    this.locationName = requireNonNull(locationName, "locationName");
    this.operation = requireNonNull(operation, "operation");
    this.properties = properties;
  }

  @Nonnull
  public String getLocationName() {
    return locationName;
  }

  public void setLocationName(@Nonnull String locationName) {
    this.locationName = requireNonNull(locationName, "locationName");
  }

  @Nonnull
  public String getOperation() {
    return operation;
  }

  public void setOperation(@Nonnull String operation) {
    this.operation = requireNonNull(operation, "operation");
  }

  @Nullable
  public List<Property> getProperties() {
    return properties;
  }

  public void setProperties(@Nullable List<Property> properties) {
    this.properties = properties;
  }
}
