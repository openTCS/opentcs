/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.routing;

import javax.annotation.Nonnull;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Computes the weight of edges in the routing graph.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface EdgeEvaluator {

  /**
   * Called when/before computation of a routing graph starts.
   *
   * @param vehicle The vehicle for which the routing graph is computed.
   */
  @ScheduledApiChange(details = "Default implementation will be removed.", when = "6.0")
  default void onGraphComputationStart(@Nonnull Vehicle vehicle) {
  }

  /**
   * Called when/after a computation of a routing graph is done.
   *
   * @param vehicle The vehicle for which the routing graph is computed.
   */
  @ScheduledApiChange(details = "Default implementation will be removed.", when = "6.0")
  default void onGraphComputationEnd(@Nonnull Vehicle vehicle) {
  }

  /**
   * Computes the weight of an edge in the routing graph.
   *
   * @param edge The edge.
   * @param vehicle The vehicle for which to compute the edge's weight.
   * @return The computed weight of the given edge.
   * A value of {@code Double.POSITIVE_INFINITY} indicates that the edge is to be excluded from
   * routing.
   * Note that negative weights might not be handled well by the respective routing algorithm used.
   */
  double computeWeight(@Nonnull Edge edge, @Nonnull Vehicle vehicle);
}
