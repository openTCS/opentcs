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
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A set of parameters for a connection to the kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ConnectionParamSet {

  /**
   * The property we expect the host to connect to in.
   */
  public static final String PROP_KERNEL_HOST = "opentcs.kernel.host";
  /**
   * The property we expect the port to connect to in.
   */
  public static final String PROP_KERNEL_PORT = "opentcs.kernel.port";
  /**
   * This class's logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(ConnectionParamSet.class);
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
    this.host = Objects.requireNonNull(host);
    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException("port out of range: " + port);
    }
    this.port = port;
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

  /**
   * Returns a parameter set with the host and port taken from the given set of
   * properties.
   *
   * @param props The properties to be used.
   * @return A parameter set with the host and port taken from the given set of
   * properties, or <code>null</code>, if the properties set did not contain a
   * host name or contained an unparsable port number. If a property containing
   * a host name is found but a property containing a port number isn't, the
   * default RMI registry port (1099) will be used.
   */
  public static ConnectionParamSet getParamSet(Properties props) {
    Objects.requireNonNull(props);

    String propHost = props.getProperty(PROP_KERNEL_HOST);
    if (propHost == null || propHost.isEmpty()) {
      return null;
    }
    String propPort = props.getProperty(PROP_KERNEL_PORT);
    int port;
    if (propPort == null || propPort.isEmpty()) {
      port = Registry.REGISTRY_PORT;
    }
    else {
      try {
        port = Integer.parseInt(propPort);
      }
      catch (NumberFormatException exc) {
        log.warn("Exception parsing port number", exc);
        return null;
      }
    }
    return new ConnectionParamSet(propHost, port);
  }
}
