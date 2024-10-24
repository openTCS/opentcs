// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.workingset;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.Set;
import org.opentcs.components.kernel.OrderSequenceCleanupApproval;
import org.opentcs.data.order.OrderSequence;

/**
 * A collection of {@link OrderSequenceCleanupApproval}s.
 */
public class CompositeOrderSequenceCleanupApproval
    implements
      OrderSequenceCleanupApproval {

  private final Set<OrderSequenceCleanupApproval> sequenceCleanupApprovals;
  private final DefaultOrderSequenceCleanupApproval defaultOrderSequenceCleanupApproval;

  /**
   * Creates a new instance.
   *
   * @param sequenceCleanupApprovals The {@link OrderSequenceCleanupApproval}s.
   * @param defaultOrderSequenceCleanupApproval The {@link OrderSequenceCleanupApproval}, which
   * should always be applied by default.
   */
  @Inject
  public CompositeOrderSequenceCleanupApproval(
      Set<OrderSequenceCleanupApproval> sequenceCleanupApprovals,
      DefaultOrderSequenceCleanupApproval defaultOrderSequenceCleanupApproval
  ) {
    this.sequenceCleanupApprovals = requireNonNull(
        sequenceCleanupApprovals,
        "sequenceCleanupApprovals"
    );
    this.defaultOrderSequenceCleanupApproval
        = requireNonNull(
            defaultOrderSequenceCleanupApproval,
            "defaultOrderSequenceCleanupApproval"
        );
  }

  @Override
  public boolean test(OrderSequence seq) {
    if (!defaultOrderSequenceCleanupApproval.test(seq)) {
      return false;
    }
    for (OrderSequenceCleanupApproval approval : sequenceCleanupApprovals) {
      if (!approval.test(seq)) {
        return false;
      }
    }
    return true;
  }
}
