/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import javax.annotation.Nullable;

/**
 * This interface declares methods that a vehicle driver intended for simulation
 * must implement.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface SimVehicleCommAdapter
    extends VehicleCommAdapter {

  /**
   * Sets an initial vehicle position.
   * This method should not be called while the communication adapter is
   * simulating order execution for the attached vehicle; the resulting
   * behaviour is undefined.
   *
   * @param newPos The new position.
   */
  void initVehiclePosition(@Nullable String newPos);
}
