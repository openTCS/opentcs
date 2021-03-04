/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import static com.google.common.base.Preconditions.checkState;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import static org.opentcs.util.Assertions.checkArgument;
import org.opentcs.virtualvehicle.LoopbackCommunicationAdapterDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry for all communication adapters in the system.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleCommAdapterRegistry
    implements Lifecycle {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VehicleCommAdapterRegistry.class);
  /**
   * The registered factories. Uses a comparator to sort the loopback driver to the end.
   */
  private final Map<VehicleCommAdapterDescription, VehicleCommAdapterFactory> factories
      = new TreeMap<>((f1, f2) -> {
        if (f1 instanceof LoopbackCommunicationAdapterDescription
            && f2 instanceof LoopbackCommunicationAdapterDescription) {
          return 0;
        }
        if (f1 instanceof LoopbackCommunicationAdapterDescription) {
          return 1;
        }
        else if (f2 instanceof LoopbackCommunicationAdapterDescription) {
          return -1;
        }
        return f1.getDescription().compareTo(f2.getDescription());
      });
  /**
   * Indicates whether this component is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new registry.
   *
   * @param kernel A reference to the local kernel.
   * @param factories The comm adapter factories.
   */
  @Inject
  public VehicleCommAdapterRegistry(LocalKernel kernel, Set<VehicleCommAdapterFactory> factories) {
    requireNonNull(kernel, "kernel");

    for (VehicleCommAdapterFactory factory : factories) {
      LOG.info("Setting up communication adapter factory: {}", factory.getClass().getName());
      this.factories.put(factory.getDescription(), factory);
    }

    checkState(!factories.isEmpty(), "No adapter factories found.");
  }

  @Override
  public void initialize() {
    if (initialized) {
      LOG.debug("Already initialized.");
      return;
    }
    for (VehicleCommAdapterFactory factory : factories.values()) {
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
      LOG.debug("Not initialized.");
      return;
    }
    for (VehicleCommAdapterFactory factory : factories.values()) {
      factory.terminate();
    }
    initialized = false;
  }

  /**
   * Returns all registered factories that can provide communication adapters.
   *
   * @return All registered factories that can provide communication adapters.
   */
  public List<VehicleCommAdapterFactory> getFactories() {
    return new LinkedList<>(factories.values());
  }

  /**
   * Returns the factory for the given description.
   *
   * @param description The description to get the factory for.
   * @return The factory for the given description.
   */
  public VehicleCommAdapterFactory findFactoryFor(VehicleCommAdapterDescription description) {
    requireNonNull(description, "description");
    checkArgument(factories.get(description) != null,
                  "No factory for description %s",
                  description);

    return factories.get(description);
  }

  /**
   * Returns a set of factories that can provide communication adapters for the
   * given vehicle.
   *
   * @param vehicle The vehicle to find communication adapters/factories for.
   * @return A set of factories that can provide communication adapters for the
   * given vehicle.
   */
  public List<VehicleCommAdapterFactory> findFactoriesFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    return factories.values().stream()
        .filter((factory) -> factory.providesAdapterFor(vehicle))
        .collect(Collectors.toList());
  }
}
