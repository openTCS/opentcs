// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A factory for <code>VehicleManager</code> instances.
 */
public interface VehicleControllerFactory {

  /**
   * Creates a new vehicle controller for the given vehicle and communication adapter.
   *
   * @param vehicle The vehicle.
   * @param commAdapter The communication adapter.
   * @return A new vehicle controller.
   */
  DefaultVehicleController createVehicleController(Vehicle vehicle, VehicleCommAdapter commAdapter);
}
