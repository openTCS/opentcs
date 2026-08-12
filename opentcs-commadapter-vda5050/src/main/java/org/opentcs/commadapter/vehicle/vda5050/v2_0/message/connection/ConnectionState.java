// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.connection;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the different state of a connection between an AGV and a message broker.
 */
public enum ConnectionState {

  /**
   * Connection between AGV and broker is active.
   */
  @JsonProperty(value = "ONLINE")
  ONLINE,
  /**
   * Connection between AGV and broker has gone offline in a coordinated way.
   */
  @JsonProperty(value = "OFFLINE")
  OFFLINE,
  /**
   * Connection between AGV and broker has unexpectedly ended.
   */
  @JsonProperty(value = "CONNECTIONBROKEN")
  CONNECTIONBROKEN;
}
