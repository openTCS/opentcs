// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opentcs.data.order.Route;

/**
 * Declares the methods a route selector must implement.
 */
public interface RouteSelector {

  /**
   * Selects a route from a given set of routes.
   *
   * @param routes The set to select a route from.
   * @return The selected route, or the empty optional, if no route was selected.
   */
  Optional<Route> select(Set<Route> routes);

  /**
   * Selects a route sequence from a given set of route sequences.
   * <p>
   * Each list entry in a route sequence represents a route for a specific drive order, where the
   * list index corresponds to the drive order index.
   * </p>
   *
   * @param routes The set of route sequences to select a route sequence from.
   * @return The selected route sequence, or an empty optional, if no route sequence was selected.
   */
  Optional<List<Route>> selectSequence(Set<List<Route>> routes);
}
