// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.Collection;
import org.jgrapht.Graph;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.components.kernel.routing.RoutingContext;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Translates model data to weighted graphs.
 */
public interface ModelGraphMapper {

  /**
   * Translates the given points and paths to a weighted graph.
   *
   * @param points The points of the model.
   * @param paths The paths of the model.
   * @param vehicle The vehicle for which to build the graph.
   * @return A weighted graph representing the topology to be used for the given vehicle.
   */
  Graph<Vertex, Edge> translateModel(
      Collection<Point> points,
      Collection<Path> paths,
      Vehicle vehicle
  );

  /**
   * Re-translates the given paths and replaces corresponding edges in the provided graph.
   * <p>
   * Translated edges that should be excluded from the graph (indicated by edge weights that match
   * {@link Double#isInfinite()} or {@link Double#isNaN()}) will be removed from the graph.
   * </p>
   *
   * @param paths The paths to re-translate.
   * @param vehicle The vehicle for which to update the graph.
   * @param graph The graph to modify.
   * @return The modified graph.
   */
  Graph<Vertex, Edge> updateGraph(
      Collection<Path> paths,
      Vehicle vehicle,
      Graph<Vertex, Edge> graph
  );

  /**
   * Indicates whether parallel model graph mapping is supported.
   *
   * @return {@code true}, if parallel mapping is supported, or {@code false}, if only sequential
   * mapping is supported.
   */
  boolean isParallelMappingSupported();

  /**
   * Called when the routing context has been updated.
   *
   * @param context The routing context.
   */
  void onRoutingContextUpdated(RoutingContext context);
}
