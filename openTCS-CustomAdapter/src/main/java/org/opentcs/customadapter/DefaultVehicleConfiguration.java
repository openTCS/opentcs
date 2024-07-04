package org.opentcs.customadapter;

/**
 * Default implementation of VehicleConfiguration interface.
 * Provides basic settings for vehicles without specific configurations.
 */
public class DefaultVehicleConfiguration
    implements
      VehicleConfigurationInterface {

  /**
   * The default host.
   */
  private String host = "localhost";
  /**
   * The default port.
   */
  private int port = 502;
  /**
   * The default communication strategy.
   */
  private String communicationStrategy = "ModbusTCP";

  /**
   * Creates a new instance.
   */
  public DefaultVehicleConfiguration() {
    // Do nothing
  }

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

  /**
   * Sets the host.
   *
   * @param host The host to set.
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * Sets the port.
   *
   * @param port The port to set.
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Sets the communication strategy.
   *
   * @param communicationStrategy The communication strategy to set.
   */
  public void setCommunicationStrategy(String communicationStrategy) {
    this.communicationStrategy = communicationStrategy;
  }
}
