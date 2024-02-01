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
import org.opentcs.components.kernel.TransportOrderCleanupApproval;
import org.opentcs.data.order.TransportOrder;

/**
 * A collection of {@link TransportOrderCleanupApproval}s.
 */
public class CompositeTransportOrderCleanupApproval
    implements TransportOrderCleanupApproval {

  private final Set<TransportOrderCleanupApproval> orderCleanupApprovals;
  private final DefaultTransportOrderCleanupApproval defaultTransportOrderCleanupApproval;

  /**
   * Creates a new instance.
   *
   * @param orderCleanupApprovals The {@link TransportOrderCleanupApproval}s.
   * @param defaultTransportOrderCleanupApproval The {@link TransportOrderCleanupApproval}, which
   * should always be applied by default.
   */
  @Inject
  public CompositeTransportOrderCleanupApproval(
      Set<TransportOrderCleanupApproval> orderCleanupApprovals,
      DefaultTransportOrderCleanupApproval defaultTransportOrderCleanupApproval) {
    this.orderCleanupApprovals = requireNonNull(orderCleanupApprovals, "orderCleanupApprovals");
    this.defaultTransportOrderCleanupApproval
        = requireNonNull(defaultTransportOrderCleanupApproval,
                         "defaultTransportOrderCleanupApproval");
  }

  @Override
  public boolean test(TransportOrder order) {
    if (!defaultTransportOrderCleanupApproval.test(order)) {
      return false;
    }
    for (TransportOrderCleanupApproval approval : orderCleanupApprovals) {
      if (!approval.test(order)) {
        return false;
      }
    }
    return true;
  }
}
