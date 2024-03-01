/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.strategies.basic.routing.PointRouter;

/**
 * Creates {@link PointRouter} instances based on the Bellman-Ford algorithm.
 */
public class BellmanFordPointRouterFactory
    extends AbstractPointRouterFactory {

  /**
   * Creates a new instance.
   *
   * @param graphProvider Provides routing graphs for vehicles.
   */
  @Inject
  public BellmanFordPointRouterFactory(@Nonnull GraphProvider graphProvider) {
    super(graphProvider);
  }

  @Override
  protected ShortestPathAlgorithm<String, Edge> createShortestPathAlgorithm(
      Graph<String, Edge> graph) {
    return new BellmanFordShortestPath<>(graph);
  }

}
