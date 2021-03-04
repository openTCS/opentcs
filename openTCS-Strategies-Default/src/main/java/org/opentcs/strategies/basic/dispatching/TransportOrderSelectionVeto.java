/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import java.util.function.Predicate;
import org.opentcs.data.order.TransportOrder;

/**
 * A predicate for {@link TransportOrder}s.
 * Returns {@code true} if the given {@link TransportOrder} should NOT be processed, yet.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface TransportOrderSelectionVeto
    extends Predicate<TransportOrder> {
}
