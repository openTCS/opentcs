/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.gui.dialog;

import java.rmi.registry.Registry;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkArgument;
import static org.opentcs.util.Assertions.checkInRange;

/**
 * A set of parameters for a connection to the portal.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ConnectionParamSet {

  /**
   * The description.
   */
  private final String description;
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
   * @param description The description for this connection.
   * @param host The host to be used.
   * @param port The port number to be used.
   * @throws IllegalArgumentException If the port number is out of the range of valid port numbers.
   */
  public ConnectionParamSet(String description, String host, int port) {
    this.description = requireNonNull(description, "description");
    this.host = requireNonNull(host);
    this.port = checkInRange(port, 0, 65535, "port");
  }

  /**
   * Creates a new instance.
   *
   * @param description The description for this connection.
   * @param host The host to be used.
   * @param port The port number to be used.
   * @throws NumberFormatException If the port string does not contain a parseable port number.
   * @throws IllegalArgumentException If the port number is out of the range of valid port numbers.
   */
  public ConnectionParamSet(String description, String host, String port)
      throws NumberFormatException, IllegalArgumentException {
    this(description, host, Integer.parseInt(port));
  }

  /**
   * Creates a new instance with the description, host and port parsed from the given string, which
   * must follow a pattern of "description:host:port".
   *
   * @param paramString
   */
  public ConnectionParamSet(String paramString) {
    requireNonNull(paramString, "paramString");
    String[] split = paramString.split("\\|", 3);
    checkArgument(split.length == 3,
                  "Could not parse input as 'description:host:port': %s",
                  paramString);
    this.description = split[0];
    this.host = split[1];
    this.port = checkInRange(Integer.parseInt(split[2]), 0, 65535, "port");
  }

  /**
   * Creates a new instance for host "localhost" and port 1099.
   */
  public ConnectionParamSet() {
    this("Localhost", "localhost", Registry.REGISTRY_PORT);
  }

  /**
   * Returns the description.
   * 
   * @return The description.
   */
  public String getDescription() {
    return description;
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
    return description.equals(other.description) && host.equals(other.host) && port == other.port;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 23 * hash + Objects.hashCode(this.description);
    hash = 23 * hash + Objects.hashCode(this.host);
    hash = 23 * hash + this.port;
    return hash;
  }

  @Override
  public String toString() {
    return getDescription() + " - " + getHost() + ":" + getPort();
  }
}
