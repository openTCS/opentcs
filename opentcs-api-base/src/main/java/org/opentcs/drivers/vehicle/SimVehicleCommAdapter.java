// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle;

import jakarta.annotation.Nullable;

/**
 * This interface declares methods that a vehicle driver intended for simulation
 * must implement.
 */
public interface SimVehicleCommAdapter
    extends
      VehicleCommAdapter {

  /**
   * Sets an initial vehicle position.
   * This method should not be called while the communication adapter is
   * simulating order execution for the attached vehicle; the resulting
   * behaviour is undefined.
   *
   * @param newPos The new position.
   */
  void initVehiclePosition(
      @Nullable
      String newPos
  );
}
