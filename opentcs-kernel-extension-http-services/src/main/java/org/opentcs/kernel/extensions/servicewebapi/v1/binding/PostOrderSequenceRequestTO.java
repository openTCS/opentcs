// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * An order sequence to be created by the kernel.
 */
public class PostOrderSequenceRequestTO {

  private boolean incompleteName;

  @Nullable
  private String type;

  @Nullable
  private List<String> orderTypes;

  @Nullable
  private String intendedVehicle;

  private boolean failureFatal;

  @Nonnull
  private List<Property> properties = List.of();

  public PostOrderSequenceRequestTO() {

  }

  public boolean isIncompleteName() {
    return incompleteName;
  }

  public PostOrderSequenceRequestTO setIncompleteName(boolean incompleteName) {
    this.incompleteName = incompleteName;
    return this;
  }

  @Nullable
  public String getType() {
    return type;
  }

  public PostOrderSequenceRequestTO setType(
      @Nullable
      String type
  ) {
    this.type = type;
    return this;
  }

  @Nullable
  public List<String> getOrderTypes() {
    return orderTypes;
  }

  public PostOrderSequenceRequestTO setOrderTypes(
      @Nullable
      List<String> orderTypes
  ) {
    this.orderTypes = orderTypes;
    return this;
  }

  @Nullable
  public String getIntendedVehicle() {
    return intendedVehicle;
  }

  public PostOrderSequenceRequestTO setIntendedVehicle(
      @Nullable
      String intendedVehicle
  ) {
    this.intendedVehicle = intendedVehicle;
    return this;
  }

  public boolean isFailureFatal() {
    return failureFatal;
  }

  public PostOrderSequenceRequestTO setFailureFatal(boolean failureFatal) {
    this.failureFatal = failureFatal;
    return this;
  }

  @Nonnull
  public List<Property> getProperties() {
    return properties;
  }

  public PostOrderSequenceRequestTO setProperties(
      @Nonnull
      List<Property> properties
  ) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

}
