/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * An order sequence to be created by the kernel.
 */
public class PostOrderSequenceRequestTO {

  private boolean incompleteName;

  @Nonnull
  private String type = OrderConstants.TYPE_NONE;

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

  @Nonnull
  public String getType() {
    return type;
  }

  public PostOrderSequenceRequestTO setType(@Nonnull String type) {
    this.type = requireNonNull(type, "type");
    return this;
  }

  @Nullable
  public String getIntendedVehicle() {
    return intendedVehicle;
  }

  public PostOrderSequenceRequestTO setIntendedVehicle(@Nullable String intendedVehicle) {
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

  public PostOrderSequenceRequestTO setProperties(@Nonnull List<Property> properties) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

}
