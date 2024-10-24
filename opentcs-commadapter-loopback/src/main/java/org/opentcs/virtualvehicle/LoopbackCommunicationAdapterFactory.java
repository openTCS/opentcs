// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;

/**
 * A factory for loopback communication adapters (virtual vehicles).
 */
public class LoopbackCommunicationAdapterFactory
    implements
      VehicleCommAdapterFactory {

  /**
   * The adapter components factory.
   */
  private final LoopbackAdapterComponentsFactory adapterFactory;
  /**
   * Indicates whether this component is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new factory.
   *
   * @param componentsFactory The adapter components factory.
   */
  @Inject
  public LoopbackCommunicationAdapterFactory(LoopbackAdapterComponentsFactory componentsFactory) {
    this.adapterFactory = requireNonNull(componentsFactory, "componentsFactory");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
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
      return;
    }
    initialized = false;
  }

  @Override
  public VehicleCommAdapterDescription getDescription() {
    return new LoopbackCommunicationAdapterDescription();
  }

  @Override
  public boolean providesAdapterFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");
    return true;
  }

  @Override
  public LoopbackCommunicationAdapter getAdapterFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");
    return adapterFactory.createLoopbackCommAdapter(vehicle);
  }
}
