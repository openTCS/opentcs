/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.jgrapht.Graph;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.components.kernel.routing.GroupMapper;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Provides routing graphs for vehicles.
 * <p>
 * This provider caches computed routing graphs until it is {@link #invalidate() invalidated}.
 * </p>
 */
public class GraphProvider {

  private final TCSObjectService objectService;
  private final ModelGraphMapper defaultModelGraphMapper;
  private final GroupMapper routingGroupMapper;
  /**
   * Contains {@link GraphResult}s mapped to (vehicle) routing groups.
   */
  private final Map<String, GraphResult> graphResultsByRoutingGroup = new HashMap<>();

  /**
   * Creates a new instance.
   *
   * @param objectService The object service providing the model data.
   * @param defaultModelGraphMapper Maps the points and paths to a graph.
   * @param routingGroupMapper Used to map vehicles to their routing groups.
   */
  @Inject
  public GraphProvider(@Nonnull TCSObjectService objectService,
                       @Nonnull DefaultModelGraphMapper defaultModelGraphMapper,
                       @Nonnull GroupMapper routingGroupMapper) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.defaultModelGraphMapper = requireNonNull(defaultModelGraphMapper,
                                                  "defaultModelGraphMapper");
    this.routingGroupMapper = requireNonNull(routingGroupMapper, "routingGroupMapper");
  }

  /**
   * Invalidates any graphs that have already been calculated.
   */
  public void invalidate() {
    graphResultsByRoutingGroup.clear();
  }

  /**
   * Returns a {@link GraphResult} containing the routing graph for the given vehicle.
   *
   * @param vehicle The vehicle.
   * @return A {@link GraphResult} containing the routing graph for the given vehicle.
   */
  public GraphResult getGraphResult(Vehicle vehicle) {
    return graphResultsByRoutingGroup.computeIfAbsent(
        routingGroupMapper.apply(vehicle),
        routingGroup -> {
          Set<Point> points = objectService.fetchObjects(Point.class);
          Set<Path> paths = objectService.fetchObjects(Path.class);
          return new GraphResult(points,
                                 paths,
                                 defaultModelGraphMapper.translateModel(points, paths, vehicle));
        }
    );
  }

  /**
   * Contains the result of a graph computation.
   */
  public static class GraphResult {

    private final Set<Point> pointBase;
    private final Set<Path> pathBase;
    private final Graph<String, Edge> graph;

    /**
     * Creates a new instance.
     *
     * @param pointBase The set of points that was used to compute the given graph.
     * @param pathBase The set of paths that was used to compute the given graph.
     * @param graph The computed graph.
     */
    public GraphResult(Set<Point> pointBase, Set<Path> pathBase, Graph<String, Edge> graph) {
      this.pointBase = requireNonNull(pointBase, "pointBase");
      this.pathBase = requireNonNull(pathBase, "pathBase");
      this.graph = requireNonNull(graph, "graph");
    }

    /**
     * Returns the set of points that was used to compute the graph.
     *
     * @return The set of points that was used to compute the graph.
     */
    public Set<Point> getPointBase() {
      return pointBase;
    }

    /**
     * Returns the set of paths that was used to compute the graph.
     *
     * @return The set of paths that was used to compute the graph.
     */
    public Set<Path> getPathBase() {
      return pathBase;
    }

    /**
     * Returns the computed graph.
     *
     * @return The computed graph.
     */
    public Graph<String, Edge> getGraph() {
      return graph;
    }
  }
}
