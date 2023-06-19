/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;

/**
 * An update for a vehicle's list of allowed order types.
 */
public class PutVehicleAllowedOrderTypesTO {

  @Nonnull
  private List<String> orderTypes;

  @JsonCreator
  public PutVehicleAllowedOrderTypesTO(
      @Nonnull @JsonProperty(value = "orderTypes", required = true) List<String> orderTypes) {
    this.orderTypes = requireNonNull(orderTypes, "orderTypes");
  }

  @Nonnull
  public List<String> getOrderTypes() {
    return orderTypes;
  }

  public PutVehicleAllowedOrderTypesTO setOrderTypes(@Nonnull List<String> orderTypes) {
    this.orderTypes = requireNonNull(orderTypes, "orderTypes");
    return this;
  }
}
