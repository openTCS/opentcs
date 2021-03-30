/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.peripherals;

import javax.annotation.Nonnull;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * A callback used to inform about the successful or failed completion of jobs.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface PeripheralJobCallback {

  /**
   * Called on successful completion of a job.
   *
   * @param job The job that was successfully completed.
   */
  void peripheralJobFinished(@Nonnull PeripheralJob job);

  /**
   * Called on failed completion of a job.
   *
   * @param job The job whose completion has failed.
   */
  void peripheralJobFailed(@Nonnull PeripheralJob job);
}
