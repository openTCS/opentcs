// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opentcs.components.kernel.RouteSelector;
import org.opentcs.data.order.Route;

/**
 * Selects a route according to the lowest cost.
 */
public class LowestCostRouteSelector
    implements
      RouteSelector {

  /**
   * Creates a new instance.
   */
  public LowestCostRouteSelector() {
  }

  @Override
  public Optional<Route> select(Set<Route> routes) {
    return routes.stream()
        .min(Comparator.comparingLong(Route::getCosts));
  }

  @Override
  public Optional<List<Route>> selectSequence(Set<List<Route>> routes) {
    return routes.stream()
        .min(
            (o1, o2) -> Long.compare(
                o1.stream().mapToLong(Route::getCosts).sum(),
                o2.stream().mapToLong(Route::getCosts).sum()
            )
        );
  }
}
