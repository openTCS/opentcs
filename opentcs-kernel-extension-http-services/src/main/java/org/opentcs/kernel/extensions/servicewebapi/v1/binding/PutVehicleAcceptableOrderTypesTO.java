// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import java.util.List;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.AcceptableOrderTypeTO;

/**
 * An update for a vehicle's list of acceptable order types.
 */
public class PutVehicleAcceptableOrderTypesTO {

  @Nonnull
  private List<AcceptableOrderTypeTO> acceptableOrderTypes;

  @JsonCreator
  public PutVehicleAcceptableOrderTypesTO(
      @Nonnull
      @JsonProperty(value = "acceptableOrderTypes", required = true)
      List<AcceptableOrderTypeTO> acceptableOrderTypes
  ) {
    this.acceptableOrderTypes = requireNonNull(acceptableOrderTypes, "acceptableOrderTypes");
  }

  @Nonnull
  public List<AcceptableOrderTypeTO> getAcceptableOrderTypes() {
    return acceptableOrderTypes;
  }

  public PutVehicleAcceptableOrderTypesTO setAcceptableOrderTypes(
      @Nonnull
      List<AcceptableOrderTypeTO> acceptableOrderTypes
  ) {
    this.acceptableOrderTypes = requireNonNull(acceptableOrderTypes, "acceptableOrderTypes");
    return this;
  }
}
