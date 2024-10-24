// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.selection;

import java.util.Collection;
import java.util.function.Function;
import org.opentcs.data.order.TransportOrder;

/**
 * A filter for {@link TransportOrder}s.
 * Returns a collection of reasons for filtering the transport order.
 * If the returned collection is empty, no reason to filter it was encountered.
 */
public interface TransportOrderSelectionFilter
    extends
      Function<TransportOrder, Collection<String>> {
}
