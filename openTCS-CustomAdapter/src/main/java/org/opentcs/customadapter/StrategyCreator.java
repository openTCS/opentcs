package org.opentcs.customadapter;

import java.util.concurrent.ScheduledExecutorService;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.data.model.Vehicle;

public interface StrategyCreator {
  /**
   * Creates a {@link CustomVehicleCommAdapter} for a {@link Vehicle} with the given configuration.
   *
   * @param vehicle The vehicle to create the adapter for.
   * @param config The configuration for the adapter.
   * @param executor The executor service used for executing tasks.
   * @param plantModelService The service providing the plant model.
   * @return A new instance of {@link CustomVehicleCommAdapter}.
   */
  CustomVehicleCommAdapter createAdapter(
      Vehicle vehicle,
      VehicleConfiguration config,
      ScheduledExecutorService executor,
      PlantModelService plantModelService
  );
}
