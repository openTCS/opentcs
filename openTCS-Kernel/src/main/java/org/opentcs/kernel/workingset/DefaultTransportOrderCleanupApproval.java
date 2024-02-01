/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.util.Objects;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.TransportOrderCleanupApproval;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * Checks whether a transport order may be removed.
 */
public class DefaultTransportOrderCleanupApproval
    implements TransportOrderCleanupApproval {

  private final PeripheralJobPoolManager peripheralJobPoolManager;
  private final DefaultPeripheralJobCleanupApproval defaultPeripheralJobCleanupApproval;
  private final CreationTimeThreshold creationTimeThreshold;

  /**
   * Creates a new instance.
   *
   * @param peripheralJobPoolManager The peripheral job pool manager to be used.
   * @param defaultPeripheralJobCleanupApproval Checks whether a peripheral job may be removed.
   * @param creationTimeThreshold Keeps track of the time used to determine whether a transport
   * order should be removed (according to its creation time).
   */
  @Inject
  public DefaultTransportOrderCleanupApproval(
      PeripheralJobPoolManager peripheralJobPoolManager,
      DefaultPeripheralJobCleanupApproval defaultPeripheralJobCleanupApproval,
      CreationTimeThreshold creationTimeThreshold) {
    this.peripheralJobPoolManager = requireNonNull(peripheralJobPoolManager,
                                                   "peripheralJobPoolManager");
    this.defaultPeripheralJobCleanupApproval
        = requireNonNull(defaultPeripheralJobCleanupApproval,
                         "defaultPeripheralJobCleanupApproval");
    this.creationTimeThreshold = requireNonNull(creationTimeThreshold, "creationTimeThreshold");
  }

  @Override
  public boolean test(TransportOrder order) {
    if (!order.getState().isFinalState()) {
      return false;
    }
    if (isRelatedToJobWithNonFinalState(order)) {
      return false;
    }
    if (isRelatedToUnapprovedJob(order)) {
      return false;
    }
    if (order.getCreationTime().isAfter(creationTimeThreshold.getCurrentThreshold())) {
      return false;
    }
    return true;
  }

  private boolean isRelatedToJobWithNonFinalState(TransportOrder order) {
    return peripheralJobPoolManager.getObjectRepo()
        .getObjects(
            PeripheralJob.class,
            job -> Objects.equals(job.getRelatedTransportOrder(), order.getReference())
        )
        .stream()
        .filter(job -> !job.getState().isFinalState())
        .findAny()
        .isPresent();
  }

  private boolean isRelatedToUnapprovedJob(TransportOrder order) {
    return !(peripheralJobPoolManager.getObjectRepo().getObjects(
             PeripheralJob.class,
             job -> Objects.equals(job.getRelatedTransportOrder(), order.getReference()))
             .stream()
             .allMatch(defaultPeripheralJobCleanupApproval));
  }
}
