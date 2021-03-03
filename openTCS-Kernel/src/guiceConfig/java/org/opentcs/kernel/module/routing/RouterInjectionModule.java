/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.routing;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import java.util.Objects;
import java.util.logging.Logger;
import javax.inject.Singleton;
import org.opentcs.algorithms.Router;
import org.opentcs.strategies.basic.routing.BasicRouter;
import org.opentcs.strategies.basic.routing.RouteEvaluator;
import org.opentcs.strategies.basic.routing.RouteEvaluatorDistance;
import org.opentcs.strategies.basic.routing.RouteEvaluatorExplicit;
import org.opentcs.strategies.basic.routing.RouteEvaluatorHops;
import org.opentcs.strategies.basic.routing.RouteEvaluatorNull;
import org.opentcs.strategies.basic.routing.RouteEvaluatorTravelTime;
import org.opentcs.strategies.basic.routing.RouteEvaluatorTurns;
import org.opentcs.strategies.basic.routing.RoutingTableBuilder;
import org.opentcs.strategies.basic.routing.RoutingTableBuilderBfs;
import org.opentcs.strategies.basic.routing.RoutingTableBuilderDfs;
import org.opentcs.util.configuration.ConfigurationStore;

/**
 * A Guice module for the openTCS router implementation.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RouterInjectionModule
    extends AbstractModule {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(RouterInjectionModule.class.getName());

  @Override
  protected void configure() {
    ConfigurationStore routerConfig
        = ConfigurationStore.getStore(BasicRouter.class.getName());

    configureTableBuilder(routerConfig.getString("tableBuilderType", "BFS"));

    bindConstant()
        .annotatedWith(BasicRouter.RouteToCurrentPos.class)
        .to(routerConfig.getBoolean("routeToCurrentPosition", false));

    bind(Router.class).to(BasicRouter.class).in(Singleton.class);
  }

  @Provides
  RouteEvaluator provideRouteEvaluator() {
    ConfigurationStore routerConfig
        = ConfigurationStore.getStore(BasicRouter.class.getName());
    // hops, distance, traveltime, turns, explicit (+null)
    String costFactorsString = routerConfig.getString("routingCostFactors",
                                                      "distance, turns");
    String[] costFactors = costFactorsString.trim().toLowerCase().split("[, ]+");
    RouteEvaluator result = new RouteEvaluatorNull();
    for (String costFactor : costFactors) {
      if (costFactor == null || costFactor.isEmpty()) {
        continue;
      }
      switch (costFactor) {
        case "hops":
          result = new RouteEvaluatorHops(result);
          break;
        case "distance":
          result = new RouteEvaluatorDistance(result);
          break;
        case "traveltime":
          result = new RouteEvaluatorTravelTime(result);
          break;
        case "turns":
          ConfigurationStore turnsConfig
              = ConfigurationStore.getStore(RouteEvaluatorTurns.class.getName());
          result = new RouteEvaluatorTurns(
              result, turnsConfig.getLong("penaltyPerTurn", 5000));
          break;
        case "explicit":
          result = new RouteEvaluatorExplicit(result);
          break;
        default:
          log.warning("Illegal cost factor '" + costFactor + "', ignored.");
      }
    }
    // Make sure at least one cost factor is used.
    if (result instanceof RouteEvaluatorNull) {
      log.warning("No cost factor configured, falling back to distance.");
      result = new RouteEvaluatorDistance(result);
    }
    return result;
  }

  private void configureTableBuilder(String builderType) {
    if (Objects.equals(builderType, "DFS")) {
      configureTableBuilderDfs();
    }
    else if (Objects.equals(builderType, "BFS")) {
      configureTableBuilderBfs();
    }
    else {
      log.warning("Unknown builder type '" + builderType + "', using BFS");
      configureTableBuilderBfs();
    }
  }

  private void configureTableBuilderDfs() {
    ConfigurationStore dfsConfigStore
        = ConfigurationStore.getStore(RoutingTableBuilderBfs.class.getName());
    bindConstant()
        .annotatedWith(RoutingTableBuilderDfs.SearchDepth.class)
        .to(dfsConfigStore.getInt("searchDepth", Integer.MAX_VALUE));
    bindConstant()
        .annotatedWith(RoutingTableBuilderDfs.TerminateEarly.class)
        .to(dfsConfigStore.getBoolean("terminateEarly", true));
    bind(RoutingTableBuilder.class).to(RoutingTableBuilderDfs.class);
  }

  private void configureTableBuilderBfs() {
    ConfigurationStore bfsConfigStore
        = ConfigurationStore.getStore(RoutingTableBuilderBfs.class.getName());
    bindConstant()
        .annotatedWith(RoutingTableBuilderBfs.TerminateEarly.class)
        .to(bfsConfigStore.getBoolean("terminateEarly", true));

    bind(RoutingTableBuilder.class).to(RoutingTableBuilderBfs.class);
  }
}
