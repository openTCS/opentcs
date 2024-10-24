// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel.routing;

import jakarta.annotation.Nonnull;
import org.opentcs.data.model.Vehicle;

/**
 * Computes the weight of edges in the routing graph.
 */
public interface EdgeEvaluator {

  /**
   * Called when/before computation of a routing graph starts.
   *
   * @param vehicle The vehicle for which the routing graph is computed.
   */
  void onGraphComputationStart(
      @Nonnull
      Vehicle vehicle
  );

  /**
   * Called when/after a computation of a routing graph is done.
   *
   * @param vehicle The vehicle for which the routing graph is computed.
   */
  void onGraphComputationEnd(
      @Nonnull
      Vehicle vehicle
  );

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
  double computeWeight(
      @Nonnull
      Edge edge,
      @Nonnull
      Vehicle vehicle
  );
}
