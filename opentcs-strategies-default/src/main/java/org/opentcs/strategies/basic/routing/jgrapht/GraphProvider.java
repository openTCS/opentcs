/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
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
  private final ModelGraphMapper generalModelGraphMapper;
  private final GroupMapper routingGroupMapper;
  private final GraphMutator graphMutator;
  /**
   * Contains {@link GraphResult}s mapped to (vehicle) routing groups.
   */
  private final Map<String, GraphResult> graphResultsByRoutingGroup = new HashMap<>();
  /**
   * A cache for derived {@link GraphResult}s.
   */
  private final Map<String, GraphResult> derivedGraphResults = new WeakHashMap<>();
  /**
   * The set of points that is currently used for computing routing graphs.
   */
  private final HashedResourceSet<Point> currentPointBase
      = new HashedResourceSet<>(this::pointsHashCode);
  /**
   * The set of paths that is currently used for computing routing graphs.
   */
  private final HashedResourceSet<Path> currentPathBase
      = new HashedResourceSet<>(this::pathsHashCode);
  /**
   * The general {@link GraphResult}.
   */
  private GraphResult generalGraphResult;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service providing the model data.
   * @param defaultModelGraphMapper Maps the points and paths to a graph.
   * @param generalModelGraphMapper Maps the points and paths to a graph.
   * @param routingGroupMapper Used to map vehicles to their routing groups.
   * @param graphMutator Provides methods for mutating {@link GraphResult}s.
   */
  @Inject
  public GraphProvider(
      @Nonnull
      TCSObjectService objectService,
      @Nonnull
      GeneralModelGraphMapper generalModelGraphMapper,
      @Nonnull
      DefaultModelGraphMapper defaultModelGraphMapper,
      @Nonnull
      GroupMapper routingGroupMapper,
      @Nonnull
      GraphMutator graphMutator
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.defaultModelGraphMapper = requireNonNull(
        defaultModelGraphMapper,
        "defaultModelGraphMapper"
    );
    this.generalModelGraphMapper = requireNonNull(
        generalModelGraphMapper,
        "generalModelGraphMapper"
    );
    this.routingGroupMapper = requireNonNull(routingGroupMapper, "routingGroupMapper");
    this.graphMutator = requireNonNull(graphMutator, "graphMutator");
  }

  /**
   * Invalidates any graphs that have already been calculated.
   */
  public void invalidate() {
    currentPointBase.clear();
    currentPathBase.clear();
    graphResultsByRoutingGroup.clear();
    derivedGraphResults.clear();
    generalGraphResult = null;
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
        routingGroup -> new GraphResult(
            vehicle,
            getCurrentPointBase().getResources(),
            getCurrentPathBase().getResources(),
            Set.of(),
            Set.of(),
            defaultModelGraphMapper.translateModel(
                getCurrentPointBase().getResources(),
                getCurrentPathBase().getResources(),
                vehicle
            )
        )
    );
  }

  /**
   * Returns a {@link GraphResult} containing a general routing graph that is not affected by any
   * path properties or any configured edge evaluators.
   *
   * @return A {@link GraphResult} containing the routing graph.
   */
  public GraphResult getGeneralGraphResult() {
    if (generalGraphResult == null) {
      generalGraphResult = new GraphResult(
          new Vehicle("Dummy"),
          getCurrentPointBase().getResources(),
          getCurrentPathBase().getResources(),
          Set.of(),
          Set.of(),
          generalModelGraphMapper.translateModel(
              getCurrentPointBase().getResources(),
              getCurrentPathBase().getResources(),
              new Vehicle("Dummy")
          )
      );
    }

    return generalGraphResult;
  }

  /**
   * Returns a {@link GraphResult} that is derived from the given vehicle's "default" routing graph
   * in such a way that the given sets of points and paths are not included.
   *
   * @param vehicle The vehicle.
   * @param pointsToExclude The set of points to not include in the derived routing graph.
   * @param pathsToExclude The set of paths to not include in the derived routing graph.
   * @return The derived {@link GraphResult}.
   */
  public GraphResult getDerivedGraphResult(
      @Nonnull
      Vehicle vehicle,
      @Nonnull
      Set<Point> pointsToExclude,
      @Nonnull
      Set<Path> pathsToExclude
  ) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(pointsToExclude, "pointsToExclude");
    requireNonNull(pathsToExclude, "pathsToExclude");

    return derivedGraphResults.computeIfAbsent(
        derivedGraphResultCacheKey(vehicle, pointsToExclude, pathsToExclude),
        key -> graphMutator.deriveGraph(pointsToExclude, pathsToExclude, getGraphResult(vehicle))
    );
  }

  /**
   * Returns a {@link GraphResult} that is derived from the general routing graph in such a way
   * that the given sets of points and paths are not included.
   *
   * @param pointsToExclude The set of points to not include in the derived routing graph.
   * @param pathsToExclude The set of paths to not include in the derived routing graph.
   * @return The derived {@link GraphResult}.
   */
  public GraphResult getDerivedGeneralGraphResult(
      @Nonnull
      Set<Point> pointsToExclude,
      @Nonnull
      Set<Path> pathsToExclude
  ) {
    requireNonNull(pointsToExclude, "pointsToExclude");
    requireNonNull(pathsToExclude, "pathsToExclude");

    return graphMutator.deriveGraph(pointsToExclude, pathsToExclude, getGeneralGraphResult());
  }

  /**
   * Updates any {@link GraphResult}s that have already been calculated using the given paths.
   * <p>
   * The general graph result will not be updated as it does not consider locked paths
   * and therefore always stays the same.
   * </p>
   *
   * @param paths The paths to use for the update.
   */
  public void updateGraphResults(
      @Nonnull
      Collection<Path> paths
  ) {
    requireNonNull(paths, "paths");

    if (paths.isEmpty()) {
      return;
    }

    // Ensure the path base is up-to-date.
    getCurrentPathBase().updateResources(paths);

    for (Map.Entry<String, GraphResult> entry : Set.copyOf(graphResultsByRoutingGroup.entrySet())) {
      graphResultsByRoutingGroup.put(
          entry.getKey(),
          new GraphResult(
              entry.getValue().getVehicle(),
              entry.getValue().getPointBase(),
              getCurrentPathBase().getResources(),
              Set.of(),
              Set.of(),
              defaultModelGraphMapper.updateGraph(
                  paths,
                  entry.getValue().getVehicle(),
                  entry.getValue().getGraph()
              )
          )
      );
    }
  }

  private String derivedGraphResultCacheKey(
      Vehicle vehicle,
      Set<Point> pointsToExclude,
      Set<Path> pathsToExclude
  ) {
    // Concat the different hash code values (e.g. instead of simply calculating the sum) to
    // minimize risk of hash collisions.
    return String.format(
        "routingGroup(%s)_pointBase(%s)_pathBase(%s)_excludedPoints(%s)_excludedPaths(%s)",
        routingGroupMapper.apply(vehicle).hashCode(),
        getCurrentPointBase().getHash(),
        getCurrentPathBase().getHash(),
        pointsHashCode(pointsToExclude),
        pathsHashCode(pathsToExclude)
    );
  }

  private HashedResourceSet<Point> getCurrentPointBase() {
    if (currentPointBase.isEmpty()) {
      currentPointBase.overrideResources(objectService.fetchObjects(Point.class));
    }

    return currentPointBase;
  }

  private HashedResourceSet<Path> getCurrentPathBase() {
    if (currentPathBase.isEmpty()) {
      currentPathBase.overrideResources(objectService.fetchObjects(Path.class));
    }

    return currentPathBase;
  }

  private int pointsHashCode(Set<Point> points) {
    return points.hashCode();
  }

  private int pathsHashCode(Set<Path> paths) {
    int result = 0;

    for (Path path : paths) {
      result += Objects.hash(path.getName(), path.isLocked(), path.getProperties());
    }

    return result;
  }

  /**
   * Contains the result of a graph computation.
   */
  public static class GraphResult {

    private final Vehicle vehicle;
    private final Set<Point> pointBase;
    private final Set<Path> pathBase;
    private final Set<Point> excludedPoints;
    private final Set<Path> excludedPaths;
    private final Graph<String, Edge> graph;

    /**
     * Creates a new instance.
     *
     * @param vehicle The vehicle for which the given graph was computed.
     * @param pointBase The set of points that was used to compute the given graph.
     * @param pathBase The set of paths that was used to compute the given graph.
     * @param excludedPoints The set of points that were excluded when computing the given graph.
     * @param excludedPaths The set of paths that were excluded when computing the given graph.
     * @param graph The computed graph.
     */
    public GraphResult(
        Vehicle vehicle,
        Set<Point> pointBase,
        Set<Path> pathBase,
        Set<Point> excludedPoints,
        Set<Path> excludedPaths,
        Graph<String, Edge> graph
    ) {
      this.pointBase = Collections.unmodifiableSet(requireNonNull(pointBase, "pointBase"));
      this.pathBase = Collections.unmodifiableSet(requireNonNull(pathBase, "pathBase"));
      this.excludedPoints
          = Collections.unmodifiableSet(requireNonNull(excludedPoints, "excludedPoints"));
      this.excludedPaths
          = Collections.unmodifiableSet(requireNonNull(excludedPaths, "excludedPaths"));
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

    public Set<Point> getExcludedPoints() {
      return excludedPoints;
    }

    public Set<Path> getExcludedPaths() {
      return excludedPaths;
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
