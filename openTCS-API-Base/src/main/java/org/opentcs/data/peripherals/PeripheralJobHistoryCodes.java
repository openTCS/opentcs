/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.peripherals;

/**
 * Defines constants for basic history event codes related to peripheral jobs and documents how the
 * respective supplementary information is to be interpreted.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface PeripheralJobHistoryCodes {

  /**
   * An event code indicating a peripheral job has been created.
   * <p>
   * The history entry's supplement is empty.
   * </p>
   */
  String JOB_CREATED = "tcsHistory:jobCreated";
  /**
   * An event code indicating a peripheral job was marked as being in a final state.
   * <p>
   * The history entry's supplement is empty.
   * </p>
   */
  String JOB_REACHED_FINAL_STATE = "tcsHistory:jobReachedFinalState";
}
