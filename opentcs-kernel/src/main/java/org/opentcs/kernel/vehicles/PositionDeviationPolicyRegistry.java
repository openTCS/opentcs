// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.Set;
import org.opentcs.components.kernel.PositionDeviationPolicy;
import org.opentcs.components.kernel.PositionDeviationPolicyFactory;
import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry for position deviation policy factories.
 */
public class PositionDeviationPolicyRegistry {
  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PositionDeviationPolicyRegistry.class);
  /**
   * The available factories.
   */
  private final Set<PositionDeviationPolicyFactory> factories;
  /**
   * Our configuration.
   */
  private final VehiclePositionResolverConfiguration configuration;

  /**
   * Creates a new instance.
   *
   * @param positionDeviationPolicyFactories The available position factories.
   * @param configuration The configuration.
   */
  @Inject
  public PositionDeviationPolicyRegistry(
      @Nonnull
      Set<PositionDeviationPolicyFactory> positionDeviationPolicyFactories,
      @Nonnull
      VehiclePositionResolverConfiguration configuration
  ) {
    this.factories = requireNonNull(
        positionDeviationPolicyFactories, "positionDeviationPolicyFactories"
    );
    this.configuration = requireNonNull(configuration, "configuration");

    for (PositionDeviationPolicyFactory factory : factories) {
      LOG.info("Registered position deviation policy factory: {}", factory.getClass().getName());
    }
    if (factories.isEmpty()) {
      LOG.info("No position deviation policy factories registered.");
    }
  }

  /**
   * Returns a position deviation policy for the given vehicle.
   *
   * @param vehicle The vehicle.
   * @return A policy for the given vehicle.
   */
  @Nonnull
  public PositionDeviationPolicy getPolicyForVehicle(
      @Nonnull
      Vehicle vehicle
  ) {
    requireNonNull(vehicle, "vehicle");

    Optional<PositionDeviationPolicy> result = factories.stream()
        .map(factory -> factory.createPolicyFor(vehicle))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();

    if (result.isPresent()) {
      LOG.info(
          "Selecting position deviation policy '{}' for vehicle '{}'.",
          result.get().getClass().getName(),
          vehicle.getName()
      );
      return result.get();
    }

    LOG.info(
        "Falling back to fixed deviation policy (distance={}, angle={}) for vehicle '{}'.",
        configuration.deviationXY(),
        configuration.deviationTheta(),
        vehicle.getName()
    );

    return new FixedPositionDeviationPolicy(
        configuration.deviationXY(),
        configuration.deviationTheta()
    );
  }
}
