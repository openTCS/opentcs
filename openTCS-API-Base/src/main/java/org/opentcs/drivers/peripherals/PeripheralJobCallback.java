/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.peripherals;

import javax.annotation.Nonnull;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.util.annotations.ScheduledApiChange;

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
   * @param job The job that was successfully completed.
   * @deprecated Use {@link #peripheralJobFinished(org.opentcs.data.TCSObjectReference)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  void peripheralJobFinished(@Nonnull PeripheralJob job);

  /**
   * Called on failed completion of a job.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param job The job whose completion has failed.
   * @deprecated Use {@link #peripheralJobFailed(org.opentcs.data.TCSObjectReference)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  void peripheralJobFailed(@Nonnull PeripheralJob job);

  /**
   * Called on successful completion of a job.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param ref A reference to the peripheral job that was successfully completed.
   */
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void peripheralJobFinished(@Nonnull TCSObjectReference<PeripheralJob> ref) {
  }

  /**
   * Called on failed completion of a job.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param ref A reference to the peripheral job whose completion has failed.
   */
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void peripheralJobFailed(@Nonnull TCSObjectReference<PeripheralJob> ref) {
  }
}
