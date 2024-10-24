// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.selection.orders;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.selection.TransportOrderSelectionFilter;

/**
 * A collection of {@link TransportOrderSelectionFilter}s.
 */
public class CompositeTransportOrderSelectionFilter
    implements
      TransportOrderSelectionFilter {

  /**
   * The {@link TransportOrderSelectionFilter}s.
   */
  private final Set<TransportOrderSelectionFilter> filters;

  @Inject
  public CompositeTransportOrderSelectionFilter(Set<TransportOrderSelectionFilter> filters) {
    this.filters = requireNonNull(filters, "filters");
  }

  @Override
  public Collection<String> apply(TransportOrder order) {
    return filters.stream()
        .flatMap(filter -> filter.apply(order).stream())
        .collect(Collectors.toList());
  }
}
