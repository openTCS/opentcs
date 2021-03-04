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
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.opentcs.access.LocalKernel;
import org.opentcs.strategies.basic.routing.PointRouter;

/**
 * Creates {@link PointRouter} instances based on the Floyd-Warshall algorithm.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class FloydWarshallPointRouterFactory
    extends AbstractPointRouterFactory {

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel providing model data.
   * @param mapper Maps the plant model to a graph.
   */
  @Inject
  public FloydWarshallPointRouterFactory(@Nonnull LocalKernel kernel,
                                         @Nonnull ModelGraphMapper mapper) {
    super(kernel, mapper);
  }

  @Override
  protected ShortestPathAlgorithm<String, ModelEdge> createShortestPathAlgorithm(
      WeightedGraph<String, ModelEdge> graph) {
    return new FloydWarshallShortestPaths<>(graph);
  }

}
