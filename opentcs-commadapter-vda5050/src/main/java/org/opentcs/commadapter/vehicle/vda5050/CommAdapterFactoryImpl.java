// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterFactory.V1dot1;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterFactory.V2dot0;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Factory for creating a vda5050 capable comm adapter.
 */
public class CommAdapterFactoryImpl
    implements
      VehicleCommAdapterFactory {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CommAdapterFactoryImpl.class);
  /**
   * Configuration ID and factory map key for 1.1.
   */
  private static final String VER_STRING_1_1 = "1.1";
  /**
   * Configuration ID and factory map key for 2.0.
   */
  private static final String VER_STRING_2_0 = "2.0";
  /**
   * The version-specific factories.
   */
  private final Map<String, Vda5050CommAdapterFactory> factories = new HashMap<>();
  /**
   * This component's initialized flag.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param configuration The configuration of the comm adapter.
   * @param commAdapterFactory1dot1 The factory to create a comm adapter version 1.1.
   * @param commAdapterFactory2dot0 The factory to create a comm adapter version 2.0.
   */
  @Inject
  public CommAdapterFactoryImpl(
      CommAdapterConfiguration configuration,
      @V1dot1
      Vda5050CommAdapterFactory commAdapterFactory1dot1,
      @V2dot0
      Vda5050CommAdapterFactory commAdapterFactory2dot0
  ) {
    requireNonNull(configuration, "configuration");
    requireNonNull(commAdapterFactory1dot1, "commAdapterFactory1dot1");
    requireNonNull(commAdapterFactory2dot0, "commAdapterFactory2dot0");

    List<String> enabledVersions = configuration.enabledVersions().stream()
        .map(entry -> entry.trim())
        .collect(Collectors.toList());

    if (enabledVersions.contains(VER_STRING_1_1)) {
      LOG.info("VDA5050 1.1 communication adapter enabled.");
      factories.put(VER_STRING_1_1, commAdapterFactory1dot1);
    }
    else {
      LOG.info("VDA5050 1.1 communication adapter disabled by configuration.");
      factories.put(VER_STRING_1_1, new DisabledVda5050CommAdapterFactory());
    }

    if (enabledVersions.contains(VER_STRING_2_0)) {
      LOG.info("VDA5050 2.0 communication adapter enabled.");
      factories.put(VER_STRING_2_0, commAdapterFactory2dot0);
    }
    else {
      LOG.info("VDA5050 2.0 communication adapter disabled by configuration.");
      factories.put(VER_STRING_2_0, new DisabledVda5050CommAdapterFactory());
    }
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

  @Override
  public VehicleCommAdapterDescription getDescription() {
    return new CommAdapterDescriptionImpl();
  }

  @Override
  public boolean providesAdapterFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    return factories.values().stream().anyMatch(factory -> factory.providesAdapterFor(vehicle));
  }

  @Override
  public VehicleCommAdapter getAdapterFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    return List.of(VER_STRING_2_0, VER_STRING_1_1).stream()
        .map(version -> factories.get(version))
        .filter(factory -> factory.providesAdapterFor(vehicle))
        .map(factory -> factory.getAdapterFor(vehicle))
        .findFirst()
        .orElse(null);
  }
}
