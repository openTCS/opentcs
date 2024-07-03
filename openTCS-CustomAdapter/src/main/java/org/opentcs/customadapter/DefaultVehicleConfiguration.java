/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.customadapter;

// import java.util.HashMap;
// import java.util.Map;
// import java.util.concurrent.Executors;
// import java.util.concurrent.ScheduledExecutorService;

/**
 * Default implementation of VehicleConfiguration interface.
 * Provides basic settings for vehicles without specific configurations.
 */
public class DefaultVehicleConfiguration
    implements
      VehicleConfigurationInterface {

  private String host = "localhost";
  private int port = 502;
//  private final Map<String, String> ethernetParameters = new HashMap<>();
  private String communicationStrategy = "ModbusTCP";

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public String getCommunicationStrategy() {
    return communicationStrategy;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setCommunicationStrategy(String communicationStrategy) {
    this.communicationStrategy = communicationStrategy;
  }
}
