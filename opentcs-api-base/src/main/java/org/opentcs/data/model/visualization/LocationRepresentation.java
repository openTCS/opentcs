// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.model.visualization;

/**
 * Common location representations.
 */
public enum LocationRepresentation {

  /**
   * A (empty) location without any representation.
   */
  NONE,
  /**
   * The default representation inherited from the assigned location type.
   */
  DEFAULT,
  /**
   * A location for vehicle load transfer, generic variant.
   */
  LOAD_TRANSFER_GENERIC,
  /**
   * A location for vehicle load transfers, alternative variant 1.
   */
  LOAD_TRANSFER_ALT_1,
  /**
   * A location for vehicle load transfers, alternative variant 2.
   */
  LOAD_TRANSFER_ALT_2,
  /**
   * A location for vehicle load transfers, alternative variant 3.
   */
  LOAD_TRANSFER_ALT_3,
  /**
   * A location for vehicle load transfers, alternative variant 4.
   */
  LOAD_TRANSFER_ALT_4,
  /**
   * A location for vehicle load transfers, alternative variant 5.
   */
  LOAD_TRANSFER_ALT_5,
  /**
   * A location for some generic processing, generic variant.
   */
  WORKING_GENERIC,
  /**
   * A location for some generic processing, alternative variant 1.
   */
  WORKING_ALT_1,
  /**
   * A location for some generic processing, alternative variant 2.
   */
  WORKING_ALT_2,
  /**
   * A location for recharging a vehicle, generic variant.
   */
  RECHARGE_GENERIC,
  /**
   * A location for recharging a vehicle, alternative variant 1.
   */
  RECHARGE_ALT_1,
  /**
   * A location for recharging a vehicle, alternative variant 2.
   */
  RECHARGE_ALT_2
}
