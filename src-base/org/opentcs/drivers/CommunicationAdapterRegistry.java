/*
 * openTCS copyright information:
 * Copyright (c) 2009 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.model.Vehicle;

/**
 * A registry for all communication adapters in the system.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CommunicationAdapterRegistry {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(CommunicationAdapterRegistry.class.getName());
  /**
   * The registered factories.
   */
  private final List<CommunicationAdapterFactory> factories;
  /**
   * A reference to the kernel.
   */
  private final LocalKernel kernel;

  /**
   * Creates a new registry.
   *
   * @param kernel A reference to the local kernel.
   */
  @Inject
  @SuppressWarnings("deprecation")
  public CommunicationAdapterRegistry(LocalKernel kernel) {
    this.kernel = Objects.requireNonNull(kernel);

    // Auto-detect communication adapter factories.
    factories = new LinkedList<>();
    ServiceLoader<CommunicationAdapterFactory> factoryLoader =
        ServiceLoader.load(CommunicationAdapterFactory.class);
    for (CommunicationAdapterFactory factory : factoryLoader) {
      log.info("Setting up communication adapter factory: "
          + factory.getClass().getName());
      factory.setCommAdapterRegistry(this);
      factory.setKernel(kernel);
      factories.add(factory);
    }

    if (factories.isEmpty()) {
      throw new IllegalStateException(
          "No communication adapter factories found.");
    }
  }

  /**
   * Returns a reference to the local kernel.
   *
   * @return A reference to the local kernel.
   * @deprecated Will be removed after openTCS 2.7.
   */
  public LocalKernel getKernel() {
    return kernel;
  }

  /**
   * Returns all registered factories that can provide communication adapters.
   *
   * @return All registered factories that can provide communication adapters.
   */
  public List<CommunicationAdapterFactory> getFactories() {
    return new LinkedList<>(factories);
  }

  /**
   * Returns a set of factories that can provide communication adapters for the
   * given vehicle.
   *
   * @param vehicle The vehicle to find communication adapters/factories for.
   * @return A set of factories that can provide communication adapters for the
   * given vehicle.
   */
  public List<CommunicationAdapterFactory> findFactoriesFor(Vehicle vehicle) {
    if (vehicle == null) {
      throw new NullPointerException("vehicle is null");
    }
    List<CommunicationAdapterFactory> result = new LinkedList<>();
    for (CommunicationAdapterFactory curFactory : factories) {
      if (curFactory.providesAdapterFor(vehicle)) {
        result.add(curFactory);
      }
    }
    return result;
  }
}
