/*
 * openTCS copyright information:
 * Copyright (c) 2015 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.CommunicationAdapter;

/**
 * A factory for <code>VehicleManager</code> instances.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface VehicleManagerFactory {

  /**
   * Creates a new StandardVehicleController for the given vehicle and
   * communication adapter.
   *
   * @param vehicle The vehicle.
   * @param commAdapter The communication adapter.
   * @return A new StandardVehicleController for the given vehicle and
   * communication adapter.
   */
  StandardVehicleController createStandardVehicleController(
      Vehicle vehicle, CommunicationAdapter commAdapter);
}
