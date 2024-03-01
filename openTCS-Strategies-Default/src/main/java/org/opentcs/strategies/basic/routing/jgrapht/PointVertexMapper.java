/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.Collection;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.locationtech.jts.triangulate.quadedge.Vertex;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.data.model.Point;

/**
 * Mapper to translate a collection of {@link Point}s to {@link Vertex vertices}.
 */
public class PointVertexMapper {

  /**
   * Creates a new instance.
   */
  @Inject
  public PointVertexMapper() {
  }

  /**
   * Translates the given {@link Point}s to {@link Vertex vertices} and adds them to a copy of the
   * provided {@link Graph}.
   *
   * @param points The points to translate to vertices.
   * @param graph The graph to whose copy the translated vertices are to be added.
   * @return A copy of the provided graph extended by the translated vertices.
   */
  public Graph<String, Edge> translatePoints(Collection<Point> points,
                                             Graph<String, Edge> graph) {
    requireNonNull(points, "points");
    requireNonNull(graph, "graph");

    Graph<String, Edge> extendedGraph = new DirectedWeightedMultigraph<>(Edge.class);
    Graphs.addGraph(extendedGraph, graph);

    for (Point point : points) {
      extendedGraph.addVertex(point.getName());
    }

    return extendedGraph;
  }
}
