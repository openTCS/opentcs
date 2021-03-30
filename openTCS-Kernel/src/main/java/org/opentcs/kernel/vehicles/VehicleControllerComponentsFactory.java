/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;

/**
 * A factory for various components related to a vehicle controller.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface VehicleControllerComponentsFactory {

  /**
   * Creates a new {@link PeripheralInteractor} instance for the given vehicle.
   *
   * @param vehicleRef The vehicle.
   * @return A new peripheral interactor.
   */
  PeripheralInteractor createPeripheralInteractor(TCSObjectReference<Vehicle> vehicleRef);
}
