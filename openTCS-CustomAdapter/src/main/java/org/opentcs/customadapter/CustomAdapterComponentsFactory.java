package org.opentcs.customadapter;

import org.opentcs.data.model.Vehicle;

/**
 * A factory for various Custom specific instances.
 */
public interface CustomAdapterComponentsFactory {

  /**
   * Creates a new CustomCommunicationAdapter for the given vehicle.
   *
   * @param vehicle The vehicle.
   * @return A new CustomCommunicationAdapter for the given vehicle.
   */
  CustomVehicleCommAdapter createCustomCommAdapter(
      Vehicle vehicle
  );
}
