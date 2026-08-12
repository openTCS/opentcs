// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common;

/**
 * Expresses the extent to which an optional parameter in a message is supported by a vehicle.
 */
public enum OptionalParameterSupport {
  /**
   * The optional parameter is supported by the vehicle.
   */
  SUPPORTED,
  /**
   * The optional parameter is not supported by the vehicle.
   */
  NOT_SUPPORTED,
  /**
   * The optional parameter required by the vehicle.
   */
  REQUIRED
}
