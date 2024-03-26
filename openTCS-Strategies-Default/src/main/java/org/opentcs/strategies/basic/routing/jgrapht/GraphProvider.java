/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
          return new GraphResult(vehicle,
                                 points,
                                 paths,
                                 defaultModelGraphMapper.translateModel(points, paths, vehicle));
        }
    );
  }

  /**
   * Updates any {@link GraphResult}s that have already been calculated using the given paths.
   *
   * @param paths The paths to use for the update.
   */
  public void updateGraphResults(@Nonnull Collection<Path> paths) {
    requireNonNull(paths, "paths");

    if (paths.isEmpty() || graphResultsByRoutingGroup.isEmpty()) {
      return;
    }

    for (String routingGroup : Set.copyOf(graphResultsByRoutingGroup.keySet())) {
      GraphResult graphResult = graphResultsByRoutingGroup.get(routingGroup);

      // Ensure the new graph result's path base is up-to-date.
      Set<Path> updatedPathBase = new HashSet<>(graphResult.getPathBase());
      updatedPathBase.removeAll(paths);
      updatedPathBase.addAll(paths);

      graphResultsByRoutingGroup.put(
          routingGroup,
          new GraphResult(
              graphResult.getVehicle(),
              graphResult.getPointBase(),
              updatedPathBase,
              defaultModelGraphMapper.updateGraph(paths,
                                                  graphResult.getVehicle(),
                                                  graphResult.getGraph())
          )
      );
    }
  }

  /**
   * Contains the result of a graph computation.
   */
  public static class GraphResult {

    private final Vehicle vehicle;
    private final Set<Point> pointBase;
    private final Set<Path> pathBase;
    private final Graph<String, Edge> graph;

    /**
     * Creates a new instance.
     *
     * @param vehicle The vehicle for which the given graph was computed.
     * @param pointBase The set of points that was used to compute the given graph.
     * @param pathBase The set of paths that was used to compute the given graph.
     * @param graph The computed graph.
     */
    public GraphResult(Vehicle vehicle,
                       Set<Point> pointBase,
                       Set<Path> pathBase,
                       Graph<String, Edge> graph) {
      this.pointBase = requireNonNull(pointBase, "pointBase");
      this.pathBase = requireNonNull(pathBase, "pathBase");
      this.graph = requireNonNull(graph, "graph");
      this.vehicle = requireNonNull(vehicle, "vehicle");
    }

    /**
     * Returns the vehicle for which the graph was computed.
     *
     * @return The vehicle for which the graph was computed.
     */
    public Vehicle getVehicle() {
      return vehicle;
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
