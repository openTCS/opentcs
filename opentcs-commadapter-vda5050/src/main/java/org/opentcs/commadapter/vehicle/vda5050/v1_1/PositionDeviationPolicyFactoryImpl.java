// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opentcs.components.kernel.PositionDeviationPolicy;
import org.opentcs.components.kernel.PositionDeviationPolicyFactory;
import org.opentcs.data.model.Vehicle;

/**
 * A factory for position deviation policies for VDA5050 1.1 vehicles.
 */
public class PositionDeviationPolicyFactoryImpl
    implements
      PositionDeviationPolicyFactory {

  /**
   * Indicates whether a vehicle has all required properties to be handled by this comm adapter.
   */
  private final VehicleHasRequiredProperties hasRequiredProperties;

  /**
   * Creates a new instance.
   *
   * @param hasRequiredProperties Indicates whether a vehicle has all required properties to be
   * handled by this comm adapter.
   */
  @Inject
  public PositionDeviationPolicyFactoryImpl(VehicleHasRequiredProperties hasRequiredProperties) {
    this.hasRequiredProperties = requireNonNull(hasRequiredProperties, "hasRequiredProperties");
  }

  public Optional<PositionDeviationPolicy> createPolicyFor(
      @Nonnull
      Vehicle vehicle
  ) {
    requireNonNull(vehicle, "vehicle");

    return Optional.of(vehicle)
        .filter(hasRequiredProperties)
        .map(PositionDeviationPolicyImpl::new);
  }
}
