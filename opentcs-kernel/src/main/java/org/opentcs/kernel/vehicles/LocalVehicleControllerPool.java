// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import org.opentcs.components.Lifecycle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleControllerPool;

/**
 * Manages the attachment of vehicle controllers to vehicles and comm adapters.
 */
public interface LocalVehicleControllerPool
    extends
      VehicleControllerPool,
      Lifecycle {

  /**
   * Associates a vehicle controller with a named vehicle and a comm adapter.
   *
   * @param vehicleName The name of the vehicle.
   * @param commAdapter The communication adapter that is going to control the physical vehicle.
   */
  void attachVehicleController(String vehicleName, VehicleCommAdapter commAdapter)
      throws IllegalArgumentException;

  /**
   * Disassociates a vehicle control and a comm adapter from a vehicle.
   *
   * @param vehicleName The name of the vehicle from which to detach.
   */
  void detachVehicleController(String vehicleName);
}
