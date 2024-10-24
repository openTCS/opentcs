// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle;

import jakarta.annotation.Nonnull;

/**
 * Maintains associations between vehicles and vehicle controllers.
 */
public interface VehicleControllerPool {

  /**
   * Returns the vehicle controller associated with the vehicle with the given name.
   * If no vehicle controller is associated with it or if there is no vehicle with the given name,
   * a null-object equivalent will be returned.
   *
   * @param vehicleName The name of the vehicle for which to return the vehicle controller.
   * @return the vehicle controller associated with the vehicle with the given name, or a
   * null-object equivalent.
   */
  @Nonnull
  VehicleController getVehicleController(String vehicleName);
}
