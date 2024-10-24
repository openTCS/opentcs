// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.peripherals;

/**
 * Defines constants for basic history event codes related to peripheral jobs and documents how the
 * respective supplementary information is to be interpreted.
 */
public interface PeripheralJobHistoryCodes {

  /**
   * An event code indicating a peripheral job has been created.
   * <p>
   * The history entry's supplement is empty.
   * </p>
   */
  String JOB_CREATED = "tcs:history:jobCreated";
  /**
   * An event code indicating a peripheral job was marked as being in a final state.
   * <p>
   * The history entry's supplement is empty.
   * </p>
   */
  String JOB_REACHED_FINAL_STATE = "tcs:history:jobReachedFinalState";
}
