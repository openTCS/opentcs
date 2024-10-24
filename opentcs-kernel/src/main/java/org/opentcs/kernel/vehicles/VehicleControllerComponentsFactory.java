// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;

/**
 * A factory for various components related to a vehicle controller.
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
