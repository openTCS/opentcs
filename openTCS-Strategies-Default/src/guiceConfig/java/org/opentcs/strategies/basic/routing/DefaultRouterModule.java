/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.strategies.basic.routing.jgrapht.BellmanFordPointRouterFactory;
import org.opentcs.strategies.basic.routing.jgrapht.DefaultModelGraphMapper;
import org.opentcs.strategies.basic.routing.jgrapht.DijkstraPointRouterFactory;
import org.opentcs.strategies.basic.routing.jgrapht.EdgeEvaluator;
import org.opentcs.strategies.basic.routing.jgrapht.EdgeEvaluatorComposite;
import org.opentcs.strategies.basic.routing.jgrapht.EdgeEvaluatorDistance;
import org.opentcs.strategies.basic.routing.jgrapht.EdgeEvaluatorExplicitProperties;
import org.opentcs.strategies.basic.routing.jgrapht.EdgeEvaluatorHops;
import org.opentcs.strategies.basic.routing.jgrapht.EdgeEvaluatorTravelTime;
import org.opentcs.strategies.basic.routing.jgrapht.FloydWarshallPointRouterFactory;
import org.opentcs.strategies.basic.routing.jgrapht.ModelGraphMapper;
import org.opentcs.strategies.basic.routing.jgrapht.ShortestPathConfiguration;
import static org.opentcs.strategies.basic.routing.jgrapht.ShortestPathConfiguration.EvaluatorType.EXPLICIT;
import static org.opentcs.strategies.basic.routing.jgrapht.ShortestPathConfiguration.EvaluatorType.TRAVELTIME;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guice configuration for the default router.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultRouterModule
    extends KernelInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultRouterModule.class);

  @Override
  protected void configure() {
    configureRouterDependencies();
    bindRouter(DefaultRouter.class);
  }

  private void configureRouterDependencies() {
    bind(DefaultRouterConfiguration.class)
        .toInstance(getConfigBindingProvider().get(DefaultRouterConfiguration.PREFIX,
                                                   DefaultRouterConfiguration.class));

    ShortestPathConfiguration spConfiguration
        = getConfigBindingProvider().get(ShortestPathConfiguration.PREFIX,
                                         ShortestPathConfiguration.class);
    bind(ShortestPathConfiguration.class)
        .toInstance(spConfiguration);

    bind(ModelGraphMapper.class)
        .to(DefaultModelGraphMapper.class);

    switch (spConfiguration.algorithm()) {
      case DIJKSTRA:
        bind(PointRouterFactory.class)
            .to(DijkstraPointRouterFactory.class);
        break;
      case BELLMAN_FORD:
        bind(PointRouterFactory.class)
            .to(BellmanFordPointRouterFactory.class);
        break;
      case FLOYD_WARSHALL:
        bind(PointRouterFactory.class)
            .to(FloydWarshallPointRouterFactory.class);
        break;
      default:
        LOG.warn("Unhandled algorithm selected ({}), falling back to Dijkstra's algorithm.",
                 spConfiguration.algorithm());
        bind(PointRouterFactory.class)
            .to(DijkstraPointRouterFactory.class);
    }

    bind(EdgeEvaluator.class)
        .toProvider(() -> {
          EdgeEvaluatorComposite result = new EdgeEvaluatorComposite();
          for (ShortestPathConfiguration.EvaluatorType type : spConfiguration.edgeEvaluators()) {
            result.getComponents().add(toEdgeEvaluator(type));
          }
          // Make sure at least one evaluator is used.
          if (result.getComponents().isEmpty()) {
            LOG.warn("No edge evaluator enabled, falling back to distance-based evaluation.");
            result.getComponents().add(new EdgeEvaluatorDistance());
          }
          return result;
        });
  }

  @SuppressWarnings("deprecation")
  private EdgeEvaluator toEdgeEvaluator(ShortestPathConfiguration.EvaluatorType type) {
    switch (type) {
      case DISTANCE:
        return new EdgeEvaluatorDistance();
      case TRAVELTIME:
        return new EdgeEvaluatorTravelTime();
      case HOPS:
        return new EdgeEvaluatorHops();
      case EXPLICIT:
        return new org.opentcs.strategies.basic.routing.jgrapht.EdgeEvaluatorExplicit();
      case EXPLICIT_PROPERTIES:
        return new EdgeEvaluatorExplicitProperties();
      default:
        throw new IllegalArgumentException("Unhandled evaluator type: " + type);
    }
  }

}
