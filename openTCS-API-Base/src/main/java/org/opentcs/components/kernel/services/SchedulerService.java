/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.services;

import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SchedulerAllocationState;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Provides methods concerning the {@link Scheduler}.
 *
 * @deprecated
 */
@Deprecated
@ScheduledApiChange(when = "6.0", details = "Will be removed.")
public interface SchedulerService {

  /**
   * Returns the current state of resource allocations.
   *
   * @return The current state of resource allocations.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   * @deprecated Acquire allocations from {@link Vehicle}, instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  SchedulerAllocationState fetchSchedulerAllocations()
      throws KernelRuntimeException;
}
