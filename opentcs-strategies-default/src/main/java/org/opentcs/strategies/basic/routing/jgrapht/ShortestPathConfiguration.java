// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.List;
import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the shortest path algorithm.
 */
@ConfigurationPrefix(ShortestPathConfiguration.PREFIX)
public interface ShortestPathConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "defaultrouter.shortestpath";

  @ConfigurationEntry(
      type = "String",
      description = {
          "The routing algorithm to be used. Valid values:",
          "'DIJKSTRA': Routes are computed using Dijkstra's algorithm.",
          "'BELLMAN_FORD': Routes are computed using the Bellman-Ford algorithm.",
          "'FLOYD_WARSHALL': Routes are computed using the Floyd-Warshall algorithm."},
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START
  )
  Algorithm algorithm();

  @ConfigurationEntry(
      type = "Comma-separated list of strings",
      description = {
          "The types of route evaluators/cost factors to be used.",
          "Results of multiple evaluators are added up. Valid values:",
          "'DISTANCE': A route's cost equals the sum of the lengths of its paths.",
          "'TRAVELTIME': A route's cost equals the vehicle's expected travel time.",
          "'EXPLICIT_PROPERTIES': A route's cost equals the sum of the explicitly given costs "
              + "extracted from path properties.",
          "'HOPS': A route's cost equals the number of paths it consists of.",
          "'BOUNDING_BOX': A route's cost equals 0 if the vehicle's bounding box does not protrude "
              + "beyond _any_ bounding boxes of points along the route. Otherwise, a route's cost "
              + "is considered infinitely high, resulting in the route to be effectively discarded."
      },
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START
  )
  List<String> edgeEvaluators();

  /**
   * The available algorithms.
   */
  enum Algorithm {
    /**
     * The Dijkstra algorithm.
     */
    DIJKSTRA(false),
    /**
     * The Bellman-Ford algorithm.
     */
    BELLMAN_FORD(true),
    /**
     * The Floyd-Warshall algorithm.
     */
    FLOYD_WARSHALL(false);

    private final boolean handlingNegativeCosts;

    Algorithm(boolean handlingNegativeCosts) {
      this.handlingNegativeCosts = handlingNegativeCosts;
    }

    public boolean isHandlingNegativeCosts() {
      return handlingNegativeCosts;
    }
  }
}
