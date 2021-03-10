/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.selection;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.data.order.TransportOrder;

/**
 * A collection of {@link TransportOrderSelectionFilter}s.
 * 
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class CompositeTransportOrderSelectionFilter
    implements TransportOrderSelectionFilter {

  /**
   * The {@link TransportOrderSelectionFilter}s.
   */
  private final Set<TransportOrderSelectionFilter> filters;
  
  @Inject
  public CompositeTransportOrderSelectionFilter(Set<TransportOrderSelectionFilter> filters) {
    this.filters = requireNonNull(filters, "filters");
  }

  @Override
  public boolean test(TransportOrder order) {
    boolean result = true;
    for (TransportOrderSelectionFilter filter : filters) {
      result &= filter.test(order);
    }
    return result;
  }
}
