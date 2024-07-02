package org.opentcs.customadapter;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Interface that defines vehicle configuration
 */
public interface VehicleConfigurationInterface {
  /**
   * Get the host of the vehicle.
   *
   * @return The host name or IP address of the vehicle.
   */
  String getHost();

  /**
   * Get the port of the vehicle.
   *
   * @return The port number of the vehicle.
   */
  int getPort();

  /**
   * Get the communication strategy used by the vehicle for communication.
   *
   * @return The communication strategy used by the vehicle.
   */
  String getCommunicationStrategy();
}
