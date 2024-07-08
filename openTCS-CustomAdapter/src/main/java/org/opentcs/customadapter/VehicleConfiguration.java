package org.opentcs.customadapter;

public record VehicleConfiguration(String currentStrategy, String host, int port) {
  public VehicleConfiguration {
    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException("Invalid port number, port number range is 0 - 65535");
    }
  }
}
