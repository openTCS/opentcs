// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel;

import java.util.function.Predicate;
import org.opentcs.data.order.TransportOrder;

/**
 * Implementations of this interface check whether a transport order may be removed.
 */
public interface TransportOrderCleanupApproval
    extends
      Predicate<TransportOrder> {

}
