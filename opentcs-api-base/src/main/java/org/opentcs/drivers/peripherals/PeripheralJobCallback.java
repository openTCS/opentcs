// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.peripherals;

import jakarta.annotation.Nonnull;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * A callback used to inform about the successful or failed completion of jobs.
 */
public interface PeripheralJobCallback {

  /**
   * Called on successful completion of a job.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param ref A reference to the peripheral job that was successfully completed.
   */
  void peripheralJobFinished(
      @Nonnull
      TCSObjectReference<PeripheralJob> ref
  );

  /**
   * Called on failed completion of a job.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param ref A reference to the peripheral job whose completion has failed.
   */
  void peripheralJobFailed(
      @Nonnull
      TCSObjectReference<PeripheralJob> ref
  );
}
