/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import com.google.inject.assistedinject.Assisted;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.components.kernel.routing.EdgeEvaluator;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapper to translate a collection of {@link Path}s to weighted {@link Edge}s.
 */
public class PathEdgeMapper {

  private static final Logger LOG = LoggerFactory.getLogger(PathEdgeMapper.class);
  private final EdgeEvaluator edgeEvaluator;
  private final boolean excludeLockedPaths;
  private final ShortestPathConfiguration configuration;

  /**
   * Creates a new instance.
   *
   * @param edgeEvaluator Computes the weights of single edges.
   * @param excludeLockedPaths Whether locked paths should be excluded from mapping.
   * @param configuration The configuration.
   */
  @Inject
  public PathEdgeMapper(@Nonnull @Assisted EdgeEvaluator edgeEvaluator,
                        @Assisted boolean excludeLockedPaths,
                        @Nonnull ShortestPathConfiguration configuration) {
    this.edgeEvaluator = requireNonNull(edgeEvaluator, "edgeEvaluator");
    this.excludeLockedPaths = excludeLockedPaths;
    this.configuration = requireNonNull(configuration, "configuration");
  }

  /**
   * Translates the given {@link Path}s to weighted {@link Edge}s.
   *
   * @param paths The paths to translate to edges.
   * @param vehicle The vehicle for which the edge weights are to be evaluated.
   * @return The translated edges mapped to their corresponding edge weights.
   */
  public Map<Edge, Double> translatePaths(Collection<Path> paths, Vehicle vehicle) {
    requireNonNull(paths, "paths");
    requireNonNull(vehicle, "vehicle");

    Map<Edge, Double> weightedEdges = new HashMap<>();

    boolean allowNegativeEdgeWeights = configuration.algorithm().isHandlingNegativeCosts();

    edgeEvaluator.onGraphComputationStart(vehicle);

    for (Path path : paths) {
      if (shouldAddForwardEdge(path)) {
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
          weightedEdges.put(edge, weight);
        }
      }

      if (shouldAddReverseEdge(path)) {
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
          weightedEdges.put(edge, weight);
        }
      }
    }

    edgeEvaluator.onGraphComputationEnd(vehicle);

    return weightedEdges;
  }

  /**
   * Checks whether an edge from the source of the given path to its destination should be added to
   * the graph.
   *
   * @param path The path.
   * @return <code>true</code> if and only if the edge should be added to the graph.
   */
  private boolean shouldAddForwardEdge(Path path) {
    return excludeLockedPaths ? path.isNavigableForward() : path.getMaxVelocity() != 0;
  }

  /**
   * Checks whether an edge from the destination of the given path to its source should be added to
   * the graph.
   *
   * @param path The path.
   * @return <code>true</code> if and only if the edge should be added to the graph.
   */
  private boolean shouldAddReverseEdge(Path path) {
    return excludeLockedPaths ? path.isNavigableReverse() : path.getMaxReverseVelocity() != 0;
  }
}
