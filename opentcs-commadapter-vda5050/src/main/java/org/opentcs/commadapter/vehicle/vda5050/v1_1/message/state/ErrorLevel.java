// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines various error levels.
 */
public enum ErrorLevel {

  /**
   * AGV is ready to start. (E.g. maintenance cycle expiration warning.)
   */
  @JsonProperty(value = "WARNING")
  WARNING,
  /**
   * AGV is not in running condition, user intervention required. (E.g. laser scanner is
   * contaminated.)
   */
  @JsonProperty(value = "FATAL")
  FATAL;
}
