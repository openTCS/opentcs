/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel;

import java.util.function.Predicate;
import org.opentcs.data.order.OrderSequence;

/**
 * Implementations of this interface check whether an order sequence may be removed.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface OrderSequenceCleanupApproval
    extends Predicate<OrderSequence> {

}
