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
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Mapper to translate a collection of points and paths into a weighted graph.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultModelGraphMapper
    implements ModelGraphMapper {

  /**
   * Computes the weight of single edges in the graph.
   */
  private final EdgeEvaluator edgeEvaluator;

  /**
   * Creates a new instance.
   *
   * @param edgeEvaluator Computes the weight of single edges in the graph.
   */
  @Inject
  public DefaultModelGraphMapper(EdgeEvaluator edgeEvaluator) {
    this.edgeEvaluator = requireNonNull(edgeEvaluator, "edgeEvaluator");
  }

  @Override
  public WeightedGraph<String, ModelEdge> translateModel(Collection<Point> points,
                                                         Collection<Path> paths,
                                                         Vehicle vehicle) {
    requireNonNull(points, "points");
    requireNonNull(paths, "paths");
    requireNonNull(vehicle, "vehicle");

    WeightedGraph<String, ModelEdge> graph = new DirectedWeightedMultigraph<>(ModelEdge.class);

    for (Point point : points) {
      graph.addVertex(point.getName());
    }

    for (Path path : paths) {

      if (shouldAddForwardEdge(path, vehicle)) {
        ModelEdge edge = new ModelEdge(path, false);

        graph.addEdge(path.getSourcePoint().getName(), path.getDestinationPoint().getName(), edge);

        graph.setEdgeWeight(edge, edgeEvaluator.computeWeight(edge, vehicle));
      }

      if (shouldAddReverseEdge(path, vehicle)) {
        ModelEdge edge = new ModelEdge(path, true);

        graph.addEdge(path.getDestinationPoint().getName(), path.getSourcePoint().getName(), edge);

        graph.setEdgeWeight(edge, edgeEvaluator.computeWeight(edge, vehicle));
      }

    }

    return graph;
  }

  /**
   * Returns <code>true</code> if and only if the graph should contain an edge from the source
   * of the path to its destination for the given vehicle.
   *
   * @param path The path
   * @param vehicle The vehicle
   * @return <code>true</code> if and only if the graph should contain the edge
   */
  protected boolean shouldAddForwardEdge(Path path, Vehicle vehicle) {
    return path.isNavigableForward();
  }

  /**
   * Returns <code>true</code> if and only if the graph should contain an edge from the destination
   * of the path to its source for the given vehicle.
   *
   * @param path The path
   * @param vehicle The vehicle
   * @return <code>true</code> if and only if the graph should contain the edge
   */
  protected boolean shouldAddReverseEdge(Path path, Vehicle vehicle) {
    return path.isNavigableReverse();
  }
}
