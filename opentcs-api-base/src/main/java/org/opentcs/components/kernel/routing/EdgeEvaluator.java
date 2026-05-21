// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel.routing;

import jakarta.annotation.Nonnull;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Computes the weight of edges in the routing graph.
 * <p>
 * Any routing graph computation is performed with a {@link RoutingContext}. Before the very first
 * computation, an edge evaluator is provided with the current routing context via
 * {@link #onRoutingContextUpdated(RoutingContext)}. Subsequent routing context updates always
 * precede a routing graph computation, but do not necessarily imply that the routing context has
 * actually changed.
 * </p>
 * <p>
 * Implementations that require information from the routing context in
 * {@link #computeWeight(Edge, Vehicle)} should process/prepare them in
 * {@link #onGraphComputationStart(Vehicle)}.
 * </p>
 * <p>
 * For every vehicle, the following methods are always executed in the following order:
 * </p>
 * <ol>
 * <li>{@link #onGraphComputationStart(Vehicle)}</li>
 * <li>{@link #computeWeight(Edge, Vehicle)}</li>
 * <li>{@link #onGraphComputationEnd(Vehicle)}</li>
 * </ol>
 * <h2>Support for parallelization</h2>
 * <p>
 * Implementations that want to support parallelization
 * (indicated by {@link #isParallelGraphComputationSupported()}) have to be aware that only the
 * following methods may be called in parallel (and only for different vehicles):
 * </p>
 * <ul>
 * <li>{@link #onGraphComputationStart(Vehicle)}</li>
 * <li>{@link #computeWeight(Edge, Vehicle)}</li>
 * <li>{@link #onGraphComputationEnd(Vehicle)}</li>
 * </ul>
 * <p>
 * Therefore, implementations must take into account that these methods may be executed in parallel
 * for different vehicles. Implementations must also take into account that
 * {@link #onRoutingContextUpdated(RoutingContext)}, which is <em>not</em> subject to
 * parallelization, and the other parallelized methods may be called from different threads.
 * </p>
 * <p>
 * Furthermore, implementations that want to support parallelization should only use information
 * contained in the routing context and refrain from querying the kernel for additional information
 * during computation, as this will most likely result in a deadlock situation.
 * </p>
 * <p>
 * Even if an edge evaluator indicates support for parallelization, this does not necessarily mean
 * that parallelization is actually utilized.
 * </p>
 */
public interface EdgeEvaluator {

  /**
   * Indicates whether parallel graph computation is supported.
   *
   * @return {@code true}, if parallel graph computation is supported, or {@code false}, if only
   * sequential graph computation is supported.
   */
  @ScheduledApiChange(when = "8.0", details = "Default implementation will be removed.")
  default boolean isParallelGraphComputationSupported() {
    return false;
  }

  /**
   * Called when the routing context has been updated.
   * <p>
   * Since this method is not subject to parallelization, calls to it should return quickly.
   * </p>
   *
   * @param context The routing context.
   */
  @ScheduledApiChange(when = "8.0", details = "Default implementation will be removed.")
  default void onRoutingContextUpdated(
      @Nonnull
      RoutingContext context
  ) {
  }

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
