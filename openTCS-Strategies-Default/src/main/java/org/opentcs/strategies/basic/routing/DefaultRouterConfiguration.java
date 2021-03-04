/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import java.util.List;
import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the {@link DefaultRouter}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(DefaultRouterConfiguration.PREFIX)
public interface DefaultRouterConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "defaultrouter";

  @ConfigurationEntry(
      type = "List of enums",
      description = {
        "The types of route evaluators/cost factors to be used.",
        "Results of multiple evaluators are added up. Valid values:",
        "'DISTANCE': A route's cost is the sum of the lengths of its paths.",
        "'TRAVELTIME': A route's cost is the vehicle's expected driving time to the destination.",
        "'HOPS': A route's cost is the number of paths on it.",
        "'TURNS': A route's cost is the number of turns/direction changes on it.",
        "'EXPLICIT': A route's cost is the sum of the explicitly given costs of its paths."})
  List<EvaluatorType> routeEvaluators();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to compute a route even if the vehicle is already at the destination.")
  boolean routeToCurrentPosition();

  @ConfigurationEntry(
      type = "String",
      description = "The type of table builder to be used ('BFS' or 'DFS').")
  TableBuilderType tableBuilderType();

  @ConfigurationEntry(
      type = "Long",
      description = "The costs of a turn when using the turn-based evaluator.")
  long turnCosts();

  @ConfigurationEntry(
      type = "Boolean",
      description = {
        "Whether to terminate a DFS/BFS branch early if a cheaper route is already known.",
        "Disabling this results in a complete/exhaustive but possibly time-consuming search."})
  boolean terminateSearchEarly();

  @ConfigurationEntry(
      type = "Integer",
      description = "The maximum search depth when using the DFS-based table builder.")
  int dfsMaxDepth();

  /**
   * The different routing table builder types.
   */
  enum TableBuilderType {
    BFS,
    DFS
  }

  enum EvaluatorType {
    DISTANCE,
    TRAVELTIME,
    HOPS,
    TURNS,
    EXPLICIT
  }
}
