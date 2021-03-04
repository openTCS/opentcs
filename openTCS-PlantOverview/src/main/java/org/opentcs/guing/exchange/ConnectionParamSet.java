/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange;

import java.rmi.registry.Registry;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkArgument;
import static org.opentcs.util.Assertions.checkInRange;

/**
 * A set of parameters for a connection to the kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ConnectionParamSet {

  /**
   * The host name.
   */
  private final String host;
  /**
   * The port number.
   */
  private final int port;

  /**
   * Creates a new instance.
   *
   * @param host The host to be used.
   * @param port The port number to be used.
   * @throws IllegalArgumentException If the port number is out of the range of
   * valid port numbers.
   */
  public ConnectionParamSet(String host, int port) {
    this.host = requireNonNull(host);
    this.port = checkInRange(port, 0, 65535, "port");
  }

  /**
   * Creates a new instance.
   *
   * @param host The host to be used.
   * @param port The port number to be used.
   * @throws NumberFormatException If the port string does not contain a
   * parseable port number.
   * @throws IllegalArgumentException If the port number is out of the range of
   * valid port numbers.
   */
  public ConnectionParamSet(String host, String port)
      throws NumberFormatException, IllegalArgumentException {
    this(host, Integer.parseInt(port));
  }

  /**
   * Creates a new instance with the host and port parsed from the given string, which must follow a
   * pattern of "host:port".
   *
   * @param hostAndPort 
   */
  public ConnectionParamSet(String hostAndPort) {
    requireNonNull(hostAndPort, "hostAndPort");
    String[] split = hostAndPort.split(":", 2);
    checkArgument(split.length == 2, "Could not parse input as 'host:port': %s", hostAndPort);
    this.host = split[0];
    this.port = checkInRange(Integer.parseInt(split[1]), 0, 65535, "port");
  }

  /**
   * Creates a new instance for host "localhost" and port 1099.
   */
  public ConnectionParamSet() {
    this("localhost", Registry.REGISTRY_PORT);
  }

  /**
   * Returns the host parameter.
   *
   * @return The host parameter.
   */
  public String getHost() {
    return host;
  }

  /**
   * Returns the port parameter.
   *
   * @return The port parameter.
   */
  public int getPort() {
    return port;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ConnectionParamSet)) {
      return false;
    }
    ConnectionParamSet other = (ConnectionParamSet) o;
    return host.equals(other.host) && port == other.port;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 23 * hash + Objects.hashCode(this.host);
    hash = 23 * hash + this.port;
    return hash;
  }

  @Override
  public String toString() {
    return getHost() + " - " + getPort();
  }
}
