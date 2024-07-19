package org.opentcs.customadapter;

import com.google.inject.Singleton;
import java.util.concurrent.ScheduledExecutorService;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.data.model.Vehicle;

@Singleton
public class ModbusTCPStrategy
    implements
      StrategyCreator {
  private static final String DEFAULT_RECHARGE_OPERATION = "RECHARGE";
  private static final int DEFAULT_COMMANDS_CAPACITY = 1000;

  ModbusTCPStrategy() {
  }

  @Override
  public CustomVehicleCommAdapter createAdapter(
      Vehicle vehicle,
      VehicleConfiguration config,
      ScheduledExecutorService executor,
      PlantModelService plantModelService
  ) {
    return new ModbusTCPVehicleCommAdapter(
        new CustomProcessModel(vehicle),
        DEFAULT_RECHARGE_OPERATION,
        DEFAULT_COMMANDS_CAPACITY,
        executor,
        vehicle,
        config.host(),
        config.port(),
        plantModelService
    );
  }
}
