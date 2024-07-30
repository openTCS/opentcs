package org.opentcs.peripheralcustomadapter;

public record PeripheralDeviceConfiguration(String currentStrategy, String host, int port) {
  public PeripheralDeviceConfiguration {
    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException("Invalid port number, port number range is 0 - 65535");
    }
  }
}
