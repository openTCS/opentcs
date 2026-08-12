// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the different state of an {@link ActionState}.
 */
public enum ActionStatus {

  /**
   * Waiting.
   */
  @JsonProperty(value = "WAITING")
  WAITING,
  /**
   * Initializing.
   */
  @JsonProperty(value = "INITIALIZING")
  INITIALIZING,
  /**
   * Running.
   */
  @JsonProperty(value = "RUNNING")
  RUNNING,
  /**
   * Finished.
   */
  @JsonProperty(value = "FINISHED")
  FINISHED,
  /**
   * Failed.
   */
  @JsonProperty(value = "FAILED")
  FAILED;
}
