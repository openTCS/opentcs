/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase;

import java.util.Collection;
import static java.util.Objects.requireNonNull;
import org.opentcs.data.order.TransportOrder;

/**
 * The result of a transport order filter operation.
 *
 * @author Stefan Walter (Fraunhofer IML)
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
