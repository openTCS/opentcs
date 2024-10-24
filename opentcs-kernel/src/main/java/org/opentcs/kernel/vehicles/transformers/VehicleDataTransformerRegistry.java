// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles.transformers;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkState;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.Set;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleDataTransformerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry for all vehicle data transformers in the system.
 */
public class VehicleDataTransformerRegistry
    implements
      Lifecycle {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VehicleDataTransformerRegistry.class);
  /**
   * The registered factories.
   */
  private final Set<VehicleDataTransformerFactory> factories;
  /**
   * Indicates whether this component is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new registry.
   *
   * @param factories The data transformer factories.
   */
  @Inject
  public VehicleDataTransformerRegistry(
      @Nonnull
      Set<VehicleDataTransformerFactory> factories
  ) {
    this.factories = requireNonNull(factories, "factories");

    checkState(!factories.isEmpty(), "No adapter factories found.");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      LOG.debug("Already initialized.");
      return;
    }
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      LOG.debug("Not initialized.");
      return;
    }
    initialized = false;
  }

  /**
   * Returns a factory for data transformers for the given vehicle.
   *
   * @param vehicle The vehicle to find a data transformer factory for.
   * @return A factory for data transformers for the given vehicle.
   */
  public VehicleDataTransformerFactory findFactoryFor(
      @Nonnull
      Vehicle vehicle
  ) {
    requireNonNull(vehicle, "vehicle");

    return Optional.ofNullable(vehicle.getProperty(ObjectPropConstants.VEHICLE_DATA_TRANSFORMER))
        .flatMap(
            name -> factories.stream()
                .filter(factory -> name.equals(factory.getName()))
                .filter(factory -> factory.providesTransformersFor(vehicle))
                .findAny()
        )
        .orElseGet(() -> {
          LOG.debug("Falling back to default transformer for vehicle '{}'", vehicle.getName());
          return new DefaultVehicleDataTransformerFactory();
        });
  }
}
