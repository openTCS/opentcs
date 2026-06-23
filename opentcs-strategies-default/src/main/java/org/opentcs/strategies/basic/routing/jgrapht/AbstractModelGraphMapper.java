// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.jgrapht;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.components.kernel.routing.RoutingContext;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapper to translate a collection of points and paths into a weighted graph.
 */
public abstract class AbstractModelGraphMapper
    implements
      ModelGraphMapper {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractModelGraphMapper.class);
  private final PointVertexMapper pointVertexMapper;
  private final PathEdgeMapper pathEdgeMapper;

  /**
   * Creates a new instance.
   *
   * @param pointVertexMapper Translates a collection of points to vertices.
   * @param pathEdgeMapper Translates a collection of paths to weighted edges.
   */
  public AbstractModelGraphMapper(
      @Nonnull
      PointVertexMapper pointVertexMapper,
      @Nonnull
      PathEdgeMapper pathEdgeMapper
  ) {
    this.pointVertexMapper = requireNonNull(pointVertexMapper, "pointVertextMapper");
    this.pathEdgeMapper = requireNonNull(pathEdgeMapper, "pathEdgeMapper");
  }

  @Override
  public boolean isParallelMappingSupported() {
    return pathEdgeMapper.isParallelMappingSupported();
  }

  @Override
  public void onRoutingContextUpdated(RoutingContext context) {
    pathEdgeMapper.onRoutingContextUpdated(context);
  }

  @Override
  public Graph<Vertex, Edge> translateModel(
      Collection<Point> points,
      Collection<Path> paths,
      Vehicle vehicle
  ) {
    requireNonNull(points, "points");
    requireNonNull(paths, "paths");
    requireNonNull(vehicle, "vehicle");

    LOG.debug("Translating model for {}...", vehicle.getName());
    long timeStampBefore = System.currentTimeMillis();

    Graph<Vertex, Edge> graph = new DirectedWeightedMultigraph<>(Edge.class);

    Map<String, Vertex> pointVertexMap = new HashMap<>();

    for (Vertex vertex : pointVertexMapper.translatePoints(points)) {
      graph.addVertex(vertex);
      pointVertexMap.put(vertex.getPoint().getName(), vertex);
    }

    for (Map.Entry<Edge, Double> edgeEntry : pathEdgeMapper.translatePaths(paths, vehicle)
        .entrySet()) {
      if (!Double.isInfinite(edgeEntry.getValue()) && !Double.isNaN(edgeEntry.getValue())) {
        graph.addEdge(
            pointVertexMap.get(edgeEntry.getKey().getSourceVertex()),
            pointVertexMap.get(edgeEntry.getKey().getTargetVertex()),
            edgeEntry.getKey()
        );
        graph.setEdgeWeight(edgeEntry.getKey(), edgeEntry.getValue());
      }
    }

    LOG.debug(
        "Translated model for {} in {} milliseconds.",
        vehicle.getName(),
        System.currentTimeMillis() - timeStampBefore
    );

    return graph;
  }

  @Override
  public Graph<Vertex, Edge> updateGraph(
      Collection<Path> paths,
      Vehicle vehicle,
      Graph<Vertex, Edge> graph
  ) {
    requireNonNull(paths, "paths");
    requireNonNull(vehicle, "vehicle");
    requireNonNull(graph, "graph");

    LOG.debug("Updating graph for {}...", vehicle.getName());
    long timeStampBefore = System.currentTimeMillis();

    Map<String, Vertex> pointVertexMap = new HashMap<>();
    for (Vertex vertex : graph.vertexSet()) {
      pointVertexMap.put(vertex.getPoint().getName(), vertex);
    }

    Map<Edge, Double> changedEdges = pathEdgeMapper.translatePaths(paths, vehicle);
    for (Map.Entry<Edge, Double> edgeEntry : changedEdges.entrySet()) {
      if (Double.isInfinite(edgeEntry.getValue()) || Double.isNaN(edgeEntry.getValue())) {
        graph.removeEdge(
            pointVertexMap.get(edgeEntry.getKey().getSourceVertex()),
            pointVertexMap.get(edgeEntry.getKey().getTargetVertex())
        );
      }
      else {
        graph.removeEdge(
            pointVertexMap.get(edgeEntry.getKey().getSourceVertex()),
            pointVertexMap.get(edgeEntry.getKey().getTargetVertex())
        );
        graph.addEdge(
            pointVertexMap.get(edgeEntry.getKey().getSourceVertex()),
            pointVertexMap.get(edgeEntry.getKey().getTargetVertex()),
            edgeEntry.getKey()
        );
        graph.setEdgeWeight(edgeEntry.getKey(), edgeEntry.getValue());
      }
    }

    LOG.debug(
        "Updated graph for {} in {} milliseconds.",
        vehicle.getName(),
        System.currentTimeMillis() - timeStampBefore
    );

    return graph;
  }
}
