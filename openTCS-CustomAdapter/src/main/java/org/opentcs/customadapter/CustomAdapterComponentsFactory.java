package org.opentcs.customadapter;

import org.opentcs.components.kernel.services.PeripheralService;
import org.opentcs.data.model.Vehicle;

/**
 * A factory for various Custom specific instances.
 */
public interface CustomAdapterComponentsFactory {

  /**
   * Creates a new CustomCommunicationAdapter for the given vehicle.
   *
   * @param vehicle The vehicle.
   * @param peripheralService The Peripheral Service.
   * @return A new CustomCommunicationAdapter for the given vehicle.
   */
  CustomVehicleCommAdapter createCustomCommAdapter(
      Vehicle vehicle,
      PeripheralService peripheralService
  );
}
