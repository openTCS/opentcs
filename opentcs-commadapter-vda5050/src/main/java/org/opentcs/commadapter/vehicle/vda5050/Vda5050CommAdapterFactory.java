// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050;

import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * An interface for creating a comm adapter for a specific version of the VDA5050 protocol.
 */
public interface Vda5050CommAdapterFactory {

  /**
   * Returns whether or not this factory can produce a comm adapter for a vehicle.
   *
   * @param vehicle The vehicle to create a comm adapter for.
   * @return True if this factory can create a comm adapter for the vehicle.
   */
  boolean providesAdapterFor(Vehicle vehicle);

  /**
   * Creates a comm adapter for a given vehicle.
   *
   * @param vehicle The vehicle to create a comm adapter for.
   * @return The comm adapter or null if this factory cannot create an adapter for the vehicle.
   */
  VehicleCommAdapter getAdapterFor(Vehicle vehicle);
}
