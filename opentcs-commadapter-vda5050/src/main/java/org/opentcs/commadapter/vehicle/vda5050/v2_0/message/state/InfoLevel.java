// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 */
public enum InfoLevel {

  /**
   * Used for visualization.
   */
  @JsonProperty(value = "INFO")
  INFO,
  /**
   * Used for debugging.
   */
  @JsonProperty(value = "DEBUG")
  DEBUG;
}
