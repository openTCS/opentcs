// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the operating modes of an AGV.
 */
public enum OperatingMode {

  /**
   * AGV is under full control of the master control.
   * <p>
   * AGV drives and executes actions based on orders from the master control.
   */
  @JsonProperty(value = "AUTOMATIC")
  AUTOMATIC,
  /**
   * AGV is under control of the master control.
   * <p>
   * AGV drives and executes actions based on orders from the master control. The driving speed is
   * controlled by the HMI (speed can't exceed the speed of automatic mode). The steering is under
   * automatic control (non-safe HMI possible).
   */
  @JsonProperty(value = "SEMIAUTOMATIC")
  SEMIAUTOMATIC,
  /**
   * Master control is not in control of the AGV.
   * <p>
   * Supervisor doesn't send driving order or actions to the AGV. HMI can be used to control the
   * steering and velocity and handling device of the AGV. Location of the AGV is send to the master
   * control. When AGV enters or leaves this mode, it immediately clears all the orders (safe HMI
   * required).
   */
  @JsonProperty(value = "MANUAL")
  MANUAL,
  /**
   * Master control is not in control of the AGV.
   * <p>
   * Master control doesn't send driving order or actions to the AGV. Authorized personal can
   * reconfigure the AGV.
   */
  @JsonProperty(value = "SERVICE")
  SERVICE,
  /**
   * Master control is not in control of the AGV.
   * <p>
   * Supervisor doesn't send driving order or actions to the AGV. The AGV is being taught, e.g.
   * mapping is done by a master control.
   */
  @JsonProperty(value = "TEACHIN")
  TEACHIN;
}
