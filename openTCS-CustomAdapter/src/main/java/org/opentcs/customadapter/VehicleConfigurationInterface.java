package org.opentcs.customadapter;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Interface that defines vehicle configuration
 */
public interface VehicleConfigurationInterface {
  /**
   * Get the name of the charging operation.
   *
   * @return String representation of the charging operation.
   */
  String getRechargeOperation();

  /**
   * Get the command capacity.
   *
   * @return The maximum capacity of the command queue.
   */
  int getCommandsCapacity();

  /**
   * Get the executor service.
   *
   * @return ScheduledExecutorService for executing asynchronous tasks.
   */
  ScheduledExecutorService getExecutorService();
}
