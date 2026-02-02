// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel;

import jakarta.annotation.Nonnull;
import java.util.Optional;
import org.opentcs.data.model.Vehicle;

/**
 * A factory for position deviation policies.
 */
public interface PositionDeviationPolicyFactory {

  /**
   * Returns a policy instance for the given vehicle, or an empty optional if no such policy can be
   * provided for this vehicle.
   *
   * @param vehicle The vehicle.
   * @return A policy instance for the given vehicle, or an empty optional if no such policy can be
   * provided for this vehicle.
   */
  Optional<PositionDeviationPolicy> createPolicyFor(
      @Nonnull
      Vehicle vehicle
  );
}
