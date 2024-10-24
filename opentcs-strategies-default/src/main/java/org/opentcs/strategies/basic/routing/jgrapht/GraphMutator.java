// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.jgrapht;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
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

    // Determine the derived point base and path base.
    Set<Point> derivedPointBase = new HashSet<>(baseGraph.getPointBase());
    derivedPointBase.removeAll(pointsToExclude);
    Set<Path> derivedPathBase = new HashSet<>(baseGraph.getPathBase());
    derivedPathBase.removeAll(pathsToExclude);

    Graph<String, Edge> derivedGraph = new DirectedWeightedMultigraph<>(Edge.class);

    // Determine the vertices that should be included and add them to the derived graph.
    Set<String> pointsToIncludeByName = derivedPointBase.stream()
        .map(Point::getName)
        .collect(Collectors.toSet());
    baseGraph.getGraph().vertexSet().stream()
        .filter(vertex -> pointsToIncludeByName.contains(vertex))
        .forEach(vertex -> derivedGraph.addVertex(vertex));

    // Determine the edges that should be included and add them to the derived graph.
    Set<String> pathsToIncludeByName = derivedPathBase.stream()
        .map(Path::getName)
        .collect(Collectors.toSet());
    baseGraph.getGraph().edgeSet().stream()
        .filter(edge -> pathsToIncludeByName.contains(edge.getPath().getName()))
        // Ensure that edges are only added if their source and target vertices are contained in the
        // derived graph. This is relevant when there are points to be excluded from the derived
        // graph, as adding an edge whose source or target vertex is not present in the graph will
        // result in an IllegalArgumentException.
        .filter(
            edge -> pointsToIncludeByName.contains(edge.getSourceVertex())
                && pointsToIncludeByName.contains(edge.getTargetVertex())
        )
        .forEach(edge -> {
          derivedGraph.addEdge(edge.getSourceVertex(), edge.getTargetVertex(), edge);
          derivedGraph.setEdgeWeight(edge, baseGraph.getGraph().getEdgeWeight(edge));
        });

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
