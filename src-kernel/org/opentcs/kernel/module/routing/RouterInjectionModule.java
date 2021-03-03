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
import javax.inject.Singleton;
import org.opentcs.algorithms.Router;
import org.opentcs.util.configuration.ConfigurationStore;
import org.opentcs.util.configuration.ItemConstraintBoolean;

/**
 * A Guice module for the openTCS router implementation.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RouterInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    ConfigurationStore routerConfig
        = ConfigurationStore.getStore(BasicRouter.class.getName());
    String costTypeProp = routerConfig.getEnum("costType",
                                               CostType.LENGTH_BASED.name(),
                                               CostType.class);
    CostType costType;
    try {
      costType = CostType.valueOf(costTypeProp);
    }
    catch (IllegalArgumentException exc) {
      costType = CostType.LENGTH_BASED;
    }
    switch (costType) {
      case HOP_BASED:
        bind(RouteEvaluator.class).to(RouteEvaluatorHops.class);
        break;
      case TIME_BASED:
        bind(RouteEvaluator.class).to(RouteEvaluatorTravelTime.class);
        break;
      case EXPLICIT:
        bind(RouteEvaluator.class).to(RouteEvaluatorExplicit.class);
        break;
      case LENGTH_BASED:
      default:
        bind(RouteEvaluator.class).to(RouteEvaluatorDistance.class);
    }
//    bind(RouteEvaluator.class).to(RouteEvaluatorCourseChangePenalty.class);

//    bindConstant()
//        .annotatedWith(RoutingTableBuilderDfs.SearchDepth.class)
//        .to(Integer.MAX_VALUE);
//    bindConstant()
//        .annotatedWith(RoutingTableBuilderDfs.TerminateEarly.class)
//        .to(true);
//    bind(RoutingTableBuilder.class).to(RoutingTableBuilderDfs.class);
    bindConstant()
        .annotatedWith(RoutingTableBuilderBfs.TerminateEarly.class)
        .to(true);
    bind(RoutingTableBuilder.class).to(RoutingTableBuilderBfs.class);

    boolean routeToCurrentPosition = routerConfig.getBoolean(
        "routeToCurrentPosition",
        false,
        "Whether to explicitly look for a (static or computed) route even if "
        + "the destination position is the source position",
        new ItemConstraintBoolean());
    bindConstant()
        .annotatedWith(BasicRouter.RouteToCurrentPos.class)
        .to(routeToCurrentPosition);

    bind(Router.class).to(BasicRouter.class).in(Singleton.class);
  }
}
