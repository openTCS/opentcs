// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.controlcenter.vehicles;

import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;

/**
 * A Vehicle adapter factory that creates no vehicles adapters.
 */
public class NullVehicleCommAdapterFactory
    implements
      VehicleCommAdapterFactory {

  /**
   * Creates a new instance.
   */
  public NullVehicleCommAdapterFactory() {
  }

  @Override
  public VehicleCommAdapterDescription getDescription() {
    return new NullVehicleCommAdapterDescription();
  }

  @Override
  public boolean providesAdapterFor(Vehicle vehicle) {
    return false;
  }

  @Override
  public VehicleCommAdapter getAdapterFor(Vehicle vehicle) {
    return null;
  }

  @Override
  public void initialize() {
  }

  @Override
  public boolean isInitialized() {
    return true;
  }

  @Override
  public void terminate() {
  }
}
