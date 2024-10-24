// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.opentcs.data.order.TransportOrder;

/**
 * The result of a transport order filter operation.
 */
public class OrderFilterResult {

  private final TransportOrder order;

  private final Collection<String> filterReasons;

  public OrderFilterResult(TransportOrder order, Collection<String> filterReasons) {
    this.order = requireNonNull(order, "order");
    this.filterReasons = requireNonNull(filterReasons, "filterReasons");
  }

  public TransportOrder getOrder() {
    return order;
  }

  public Collection<String> getFilterReasons() {
    return filterReasons;
  }

  public boolean isFiltered() {
    return !filterReasons.isEmpty();
  }
}
