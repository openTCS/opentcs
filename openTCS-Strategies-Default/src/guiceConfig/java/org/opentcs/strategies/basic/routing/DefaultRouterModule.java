/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import javax.inject.Singleton;
import org.opentcs.components.kernel.routing.GroupMapper;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.strategies.basic.routing.edgeevaluator.EdgeEvaluatorComposite;
import org.opentcs.strategies.basic.routing.edgeevaluator.EdgeEvaluatorDistance;
import org.opentcs.strategies.basic.routing.edgeevaluator.EdgeEvaluatorExplicitProperties;
import org.opentcs.strategies.basic.routing.edgeevaluator.EdgeEvaluatorHops;
import org.opentcs.strategies.basic.routing.edgeevaluator.EdgeEvaluatorTravelTime;
import org.opentcs.strategies.basic.routing.edgeevaluator.ExplicitPropertiesConfiguration;
import org.opentcs.strategies.basic.routing.jgrapht.BellmanFordPointRouterFactory;
import org.opentcs.strategies.basic.routing.jgrapht.DefaultModelGraphMapper;
import org.opentcs.strategies.basic.routing.jgrapht.DijkstraPointRouterFactory;
import org.opentcs.strategies.basic.routing.jgrapht.FloydWarshallPointRouterFactory;
import org.opentcs.strategies.basic.routing.jgrapht.ModelGraphMapper;
import org.opentcs.strategies.basic.routing.jgrapht.ShortestPathConfiguration;
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

    edgeEvaluatorBinder()
        .addBinding(EdgeEvaluatorDistance.CONFIGURATION_KEY)
        .to(EdgeEvaluatorDistance.class);
    edgeEvaluatorBinder()
        .addBinding(EdgeEvaluatorExplicitProperties.CONFIGURATION_KEY)
        .to(EdgeEvaluatorExplicitProperties.class);
    edgeEvaluatorBinder()
        .addBinding(EdgeEvaluatorHops.CONFIGURATION_KEY)
        .to(EdgeEvaluatorHops.class);
    edgeEvaluatorBinder()
        .addBinding(EdgeEvaluatorTravelTime.CONFIGURATION_KEY)
        .to(EdgeEvaluatorTravelTime.class);

    bind(EdgeEvaluatorComposite.class)
        .in(Singleton.class);

    bind(ExplicitPropertiesConfiguration.class)
        .toInstance(getConfigBindingProvider().get(ExplicitPropertiesConfiguration.PREFIX,
                                                   ExplicitPropertiesConfiguration.class));

    bind(DefaultRoutingGroupMapper.class)
        .in(Singleton.class);
    bind(GroupMapper.class)
        .to(DefaultRoutingGroupMapper.class);
  }
}
