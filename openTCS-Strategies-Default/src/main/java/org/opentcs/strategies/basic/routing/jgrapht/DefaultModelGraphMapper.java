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
import javax.inject.Inject;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.routing.edgeevaluator.EdgeEvaluatorComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapper to translate a collection of points and paths into a weighted graph.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultModelGraphMapper
    implements ModelGraphMapper {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultModelGraphMapper.class);
  /**
   * Computes the weight of single edges in the graph.
   */
  private final EdgeEvaluatorComposite edgeEvaluator;
  /**
   * The configuration.
   */
  private final ShortestPathConfiguration configuration;

  /**
   * Creates a new instance.
   *
   * @param edgeEvaluator Computes the weight of single edges in the graph.
   * @param configuration The configuration.
   */
  @Inject
  public DefaultModelGraphMapper(@Nonnull EdgeEvaluatorComposite edgeEvaluator,
                                 @Nonnull ShortestPathConfiguration configuration) {
    this.edgeEvaluator = requireNonNull(edgeEvaluator, "edgeEvaluator");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public Graph<String, Edge> translateModel(Collection<Point> points,
                                            Collection<Path> paths,
                                            Vehicle vehicle) {
    requireNonNull(points, "points");
    requireNonNull(paths, "paths");
    requireNonNull(vehicle, "vehicle");
    
    edgeEvaluator.onGraphComputationStart(vehicle);

    Graph<String, Edge> graph = new DirectedWeightedMultigraph<>(Edge.class);

    for (Point point : points) {
      graph.addVertex(point.getName());
    }

    boolean allowNegativeEdgeWeights = configuration.algorithm().isHandlingNegativeCosts();

    for (Path path : paths) {
      if (shouldAddForwardEdge(path, vehicle)) {
        Edge edge = new Edge(path, false);
        double weight = edgeEvaluator.computeWeight(edge, vehicle);

        if (weight < 0 && !allowNegativeEdgeWeights) {
          LOG.warn("Edge {} with weight {} ignored. Algorithm {} cannot handle negative weights.",
                   edge,
                   weight,
                   configuration.algorithm().name());
        }
        else if (weight == Double.POSITIVE_INFINITY) {
          LOG.debug("Edge {} with infinite weight ignored.", edge);
        }
        else {
          graph.addEdge(path.getSourcePoint().getName(),
                        path.getDestinationPoint().getName(),
                        edge);
          graph.setEdgeWeight(edge, weight);
        }
      }

      if (shouldAddReverseEdge(path, vehicle)) {
        Edge edge = new Edge(path, true);
        double weight = edgeEvaluator.computeWeight(edge, vehicle);

        if (weight < 0 && !allowNegativeEdgeWeights) {
          LOG.warn("Edge {} with weight {} ignored. Algorithm {} cannot handle negative weights.",
                   edge,
                   weight,
                   configuration.algorithm().name());
        }
        else if (weight == Double.POSITIVE_INFINITY) {
          LOG.debug("Edge {} with infinite weight ignored.", edge);
        }
        else {
          graph.addEdge(path.getDestinationPoint().getName(),
                        path.getSourcePoint().getName(),
                        edge);
          graph.setEdgeWeight(edge, weight);
        }
      }
    }
    
    edgeEvaluator.onGraphComputationEnd(vehicle);

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
