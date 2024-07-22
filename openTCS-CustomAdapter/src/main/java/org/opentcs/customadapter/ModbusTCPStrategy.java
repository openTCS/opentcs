package org.opentcs.customadapter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import java.util.concurrent.ScheduledExecutorService;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.data.model.Vehicle;

@Singleton
public class ModbusTCPStrategy
    implements
      StrategyCreator {
  private static final String DEFAULT_RECHARGE_OPERATION = "RECHARGE";
  private static final int DEFAULT_COMMANDS_CAPACITY = 1000;

  @Inject
  ModbusTCPStrategy() {
  }

  @Override
  public CustomVehicleCommAdapter createAdapter(
      @Assisted
      Vehicle vehicle,
      VehicleConfiguration config,
      ScheduledExecutorService executor,
      PlantModelService plantModelService
  ) {
    return new ModbusTCPVehicleCommAdapter(
        executor,
        vehicle,
        config.host(),
        config.port(),
        plantModelService
    );
  }
}
