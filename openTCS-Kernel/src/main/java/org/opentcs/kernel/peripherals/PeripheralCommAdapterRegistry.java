/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.peripherals;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.Location;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterFactory;
import static org.opentcs.util.Assertions.checkArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry for all peripheral communication adapters in the system.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralCommAdapterRegistry
    implements Lifecycle {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeripheralCommAdapterRegistry.class);
  /**
   * The registered factories.
   */
  private final Map<PeripheralCommAdapterDescription, PeripheralCommAdapterFactory> factories
      = new HashMap<>();
  /**
   * Indicates whether this component is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new registry.
   *
   * @param factories The peripheral comm adapter factories.
   */
  @Inject
  public PeripheralCommAdapterRegistry(Set<PeripheralCommAdapterFactory> factories) {
    requireNonNull(factories, "factories");
    for (PeripheralCommAdapterFactory factory : factories) {
      LOG.info("Setting up peripheral communication adapter factory: {}",
               factory.getClass().getName());
      this.factories.put(factory.getDescription(), factory);
    }
  }

  @Override
  public void initialize() {
    if (initialized) {
      return;
    }

    for (PeripheralCommAdapterFactory factory : factories.values()) {
      factory.initialize();
    }

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!initialized) {
      return;
    }

    for (PeripheralCommAdapterFactory factory : factories.values()) {
      factory.terminate();
    }

    initialized = false;
  }

  /**
   * Returns all registered factories that can provide peripheral communication adapters.
   *
   * @return All registered factories that can provide peripheral communication adapters.
   */
  public List<PeripheralCommAdapterFactory> getFactories() {
    return new LinkedList<>(factories.values());
  }

  /**
   * Returns the factory for the given description.
   *
   * @param description The description to get the factory for.
   * @return The factory for the given description.
   */
  @Nonnull
  public PeripheralCommAdapterFactory findFactoryFor(
      @Nonnull PeripheralCommAdapterDescription description) {
    requireNonNull(description, "description");
    checkArgument(factories.get(description) != null,
                  "No factory for description %s",
                  description);

    return factories.get(description);
  }

  /**
   * Returns a set of factories that can provide communication adapters for the given location.
   *
   * @param location The location to find communication adapters/factories for.
   * @return A set of factories that can provide communication adapters for the given location.
   */
  public List<PeripheralCommAdapterFactory> findFactoriesFor(Location location) {
    requireNonNull(location, "location");

    return factories.values().stream()
        .filter(factory -> factory.providesAdapterFor(location))
        .collect(Collectors.toList());
  }
}
