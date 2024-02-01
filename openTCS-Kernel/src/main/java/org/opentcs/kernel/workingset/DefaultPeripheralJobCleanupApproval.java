/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.PeripheralJobCleanupApproval;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * Checks whether a peripheral job may be removed.
 */
public class DefaultPeripheralJobCleanupApproval
    implements PeripheralJobCleanupApproval {

  private final CreationTimeThreshold creationTimeThreshold;

  /**
   * Creates a new instance.
   *
   * @param creationTimeThreshold Keeps track of the time used to determine whether a peripheral
   * job should be removed (according to its creation time).
   */
  @Inject
  public DefaultPeripheralJobCleanupApproval(CreationTimeThreshold creationTimeThreshold) {
    this.creationTimeThreshold = requireNonNull(creationTimeThreshold, "creationTimeThreshold");
  }

  @Override
  public boolean test(PeripheralJob job) {
    if (!job.getState().isFinalState()) {
      return false;
    }
    if (job.getCreationTime().isAfter(creationTimeThreshold.getCurrentThreshold())) {
      return false;
    }
    return true;
  }
}
