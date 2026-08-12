// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 */
public enum EStop {

  /**
   * Auto-acknowledgeable e-stop is activated. (E.g. by bumper or protective field.)
   */
  @JsonProperty(value = "AUTOACK")
  AUTOACK,
  /**
   * E-stop has to be acknowledged manually at the vehicle.
   */
  @JsonProperty(value = "MANUAL")
  MANUAL,
  /**
   * Facility e-stop has to be acknowledged remotely.
   */
  @JsonProperty(value = "REMOTE")
  REMOTE,
  /**
   * No e-stop activated.
   */
  @JsonProperty(value = "NONE")
  NONE;
}
