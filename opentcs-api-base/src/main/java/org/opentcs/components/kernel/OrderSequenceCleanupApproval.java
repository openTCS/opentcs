// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel;

import java.util.function.Predicate;
import org.opentcs.data.order.OrderSequence;

/**
 * Implementations of this interface check whether an order sequence may be removed.
 */
public interface OrderSequenceCleanupApproval
    extends
      Predicate<OrderSequence> {

}
