// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.jgrapht;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.graph.AbstractBaseGraph;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.strategies.basic.routing.jgrapht.GraphProvider.GraphResult;

/**
 * Provides methods for mutating {@link GraphResult}s.
 */
public class GraphMutator {

  /**
   * Creates a new instance.
   */
  public GraphMutator() {
  }

  /**
   * Creates a graph that is derived from the given base graph by excluding the given sets of points
   * and paths from the derived graph.
   *
   * @param pointsToExclude The set of points to exclude from the derived graph.
   * @param pathsToExclude The set of paths to exclude from the derived graph.
   * @param baseGraph The base graph.
   * @return The derived graph.
   */
  public GraphResult deriveGraph(
      @Nonnull
      Set<Point> pointsToExclude,
      @Nonnull
      Set<Path> pathsToExclude,
      @Nonnull
      GraphResult baseGraph
  ) {
    requireNonNull(pointsToExclude, "pointsToExclude");
    requireNonNull(pathsToExclude, "pathsToExclude");
    requireNonNull(baseGraph, "baseGraph");

    @SuppressWarnings("unchecked")
    Graph<Vertex, Edge> derivedGraph
        = (Graph<Vertex, Edge>) ((AbstractBaseGraph<Vertex, Edge>) baseGraph.getGraph()).clone();

    Set<String> pointsToExcludeByName = pointsToExclude.stream()
        .map(Point::getName)
        .collect(Collectors.toSet());
    baseGraph.getGraph().vertexSet().stream()
        .filter(vertex -> pointsToExcludeByName.contains(vertex.getPoint().getName()))
        .forEach(derivedGraph::removeVertex);

    Set<String> pathsToExcludeByName = pathsToExclude.stream()
        .map(Path::getName)
        .collect(Collectors.toSet());
    baseGraph.getGraph().edgeSet().stream()
        .filter(edge -> pathsToExcludeByName.contains(edge.getPath().getName()))
        .forEach(derivedGraph::removeEdge);

    return new GraphResult(
        baseGraph.getVehicle(),
        baseGraph.getPointBase(),
        baseGraph.getPathBase(),
        pointsToExclude,
        pathsToExclude,
        derivedGraph
    );
  }
}
