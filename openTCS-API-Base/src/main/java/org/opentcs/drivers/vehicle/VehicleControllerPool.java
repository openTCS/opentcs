/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import javax.annotation.Nonnull;

/**
 * Maintains associations between vehicles and vehicle controllers.
 *
 * @author Stefan Walter (Fraunhofer IML)
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
