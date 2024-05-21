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
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
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
   * Returns a {@link GraphResult} that is derived from the given vehicle's "default" routing graph
   * in such a way that the given set of points is not included.
   *
   * @param vehicle The vehicle.
   * @param pointsToExclude The set of points to not include in the derived routing graph.
   * @param pathsToExclude The set of paths to not include in the derived routing graph.
   * @return The derived {@link GraphResult}.
   */
  public GraphResult getDerivedGraphResult(Vehicle vehicle,
                                           Set<Point> pointsToExclude,
                                           Set<Path> pathsToExclude) {
    GraphResult baseGraph = getGraphResult(vehicle);

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

    return new GraphResult(vehicle, derivedPointBase, derivedPathBase, derivedGraph);
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
