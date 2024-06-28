package org.opentcs.customadapter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Default implementation of VehicleConfiguration interface.
 * Provides basic settings for vehicles without specific configurations.
 */
public class DefaultVehicleConfiguration implements VehicleConfigurationInterface {

  @Override
  public String getRechargeOperation() {
    return "CHARGE";
  }

  @Override
  public int getCommandsCapacity() {
    return 10;
  }

  @Override
  public ScheduledExecutorService getExecutorService() {
    return Executors.newSingleThreadScheduledExecutor();
  }
}
