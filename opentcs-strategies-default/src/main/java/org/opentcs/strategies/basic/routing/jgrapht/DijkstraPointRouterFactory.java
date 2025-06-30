// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.jgrapht;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.strategies.basic.routing.PointRouter;

/**
 * Creates {@link PointRouter} instances based on the Dijkstra algorithm.
 */
public class DijkstraPointRouterFactory
    extends
      AbstractPointRouterFactory {

  /**
   * Creates a new instance.
   *
   * @param graphProvider Provides routing graphs for vehicles.
   */
  @Inject
  public DijkstraPointRouterFactory(
      @Nonnull
      GraphProvider graphProvider
  ) {
    super(graphProvider);
  }

  @Override
  protected ShortestPathAlgorithm<Vertex, Edge> createShortestPathAlgorithm(
      Graph<Vertex, Edge> graph
  ) {
    return new DijkstraShortestPath<>(graph);
  }

}
