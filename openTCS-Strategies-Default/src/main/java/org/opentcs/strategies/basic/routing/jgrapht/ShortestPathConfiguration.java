/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.List;
import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the shortest path algorithm.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@ConfigurationPrefix(ShortestPathConfiguration.PREFIX)
public interface ShortestPathConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "defaultrouter.shortestpath";

  @ConfigurationEntry(
      type = "Strings",
      description = {
        "The routing algorithm to be used. Valid values:",
        "'DIJKSTRA': Routes are computed using Dijkstra's algorithm.",
        "'BELLMAN_FORD': Routes are computed using the Bellman-Ford algorithm.",
        "'FLOYD_WARSHALL': Routes are computed using the Floyd-Warshall algorithm."})
  Algorithm algorithm();

  @ConfigurationEntry(
      type = "List of strings",
      description = {
        "The types of route evaluators/cost factors to be used.",
        "Results of multiple evaluators are added up. Valid values:",
        "'DISTANCE': A route's cost is the sum of the lengths of its paths.",
        "'TRAVELTIME': A route's cost is the vehicle's expected driving time to the destination.",
        "'EXPLICIT': A route's cost is the sum of the explicitly given costs of its paths."})
  List<EvaluatorType> edgeEvaluators();

  enum Algorithm {
    DIJKSTRA(false),
    BELLMAN_FORD(true),
    FLOYD_WARSHALL(false);

    private final boolean handlingNegativeCosts;

    private Algorithm(boolean handlingNegativeCosts) {
      this.handlingNegativeCosts = handlingNegativeCosts;
    }

    public boolean isHandlingNegativeCosts() {
      return handlingNegativeCosts;
    }
  }

  enum EvaluatorType {
    DISTANCE,
    TRAVELTIME,
    HOPS,
    EXPLICIT
  }
}
