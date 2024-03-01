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
import javax.annotation.Nonnull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Mapper to translate a collection of points and paths into a weighted graph.
 */
public abstract class AbstractModelGraphMapper
    implements ModelGraphMapper {

  private final PointVertexMapper pointVertexMapper;
  private final PathEdgeMapper pathEdgeMapper;

  /**
   * Creates a new instance.
   *
   * @param pointVertexMapper Translates a collection of points to vertices.
   * @param pathEdgeMapper Translates a collection of paths to weighted edges.
   */
  public AbstractModelGraphMapper(@Nonnull PointVertexMapper pointVertexMapper,
                                  @Nonnull PathEdgeMapper pathEdgeMapper) {
    this.pointVertexMapper = requireNonNull(pointVertexMapper, "pointVertextMapper");
    this.pathEdgeMapper = requireNonNull(pathEdgeMapper, "pathEdgeMapper");
  }

  @Override
  public Graph<String, Edge> translateModel(Collection<Point> points,
                                            Collection<Path> paths,
                                            Vehicle vehicle) {
    requireNonNull(points, "points");
    requireNonNull(paths, "paths");
    requireNonNull(vehicle, "vehicle");

    Graph<String, Edge> graph = new DirectedWeightedMultigraph<>(Edge.class);

    graph = pointVertexMapper.translatePoints(points, graph);
    graph = pathEdgeMapper.translatePaths(paths, vehicle, graph);

    return graph;
  }
}
