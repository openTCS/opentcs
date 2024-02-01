/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.components.kernel.PeripheralJobCleanupApproval;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * A collection of {@link PeripheralJobCleanupApproval}s.
 */
public class CompositePeripheralJobCleanupApproval
    implements PeripheralJobCleanupApproval {

  private final Set<PeripheralJobCleanupApproval> peripheralJobCleanupApprovals;
  private final DefaultPeripheralJobCleanupApproval defaultPeripheralJobCleanupApproval;

  /**
   * Creates a new instance.
   *
   * @param peripheralJobCleanupApprovals The {@link PeripheralJobCleanupApproval}s.
   * @param defaultPeripheralJobCleanupApproval The {@link PeripheralJobCleanupApproval}, which
   * should always be applied by default.
   */
  @Inject
  public CompositePeripheralJobCleanupApproval(
      Set<PeripheralJobCleanupApproval> peripheralJobCleanupApprovals,
      DefaultPeripheralJobCleanupApproval defaultPeripheralJobCleanupApproval) {
    this.peripheralJobCleanupApprovals = requireNonNull(peripheralJobCleanupApprovals,
                                                        "peripheralJobCleanupApprovals");
    this.defaultPeripheralJobCleanupApproval
        = requireNonNull(defaultPeripheralJobCleanupApproval,
                         "defaultPeripheralJobCleanupApproval");
  }

  @Override
  public boolean test(PeripheralJob job) {
    if (!defaultPeripheralJobCleanupApproval.test(job)) {
      return false;
    }
    for (PeripheralJobCleanupApproval approval : peripheralJobCleanupApprovals) {
      if (!approval.test(job)) {
        return false;
      }
    }
    return true;
  }
}
