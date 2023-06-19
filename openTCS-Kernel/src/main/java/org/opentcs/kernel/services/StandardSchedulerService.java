/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.services;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.customizations.kernel.GlobalSyncObject;

/**
 * This class is the standard implementation of the {@code SchedulerService} interface.
 */
@Deprecated
public class StandardSchedulerService
    implements org.opentcs.components.kernel.services.SchedulerService {

  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The scheduler.
   */
  private final Scheduler scheduler;

  /**
   * Creates a new instance.
   *
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param scheduler The scheduler.
   */
  @Inject
  public StandardSchedulerService(@GlobalSyncObject Object globalSyncObject,
                                  Scheduler scheduler) {
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.scheduler = requireNonNull(scheduler, "scheduler");
  }

  @Override
  public org.opentcs.access.SchedulerAllocationState fetchSchedulerAllocations() {
    synchronized (globalSyncObject) {
      return new org.opentcs.access.SchedulerAllocationState(scheduler.getAllocations());
    }
  }
}
