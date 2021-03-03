/*
 * openTCS copyright information:
 * Copyright (c) 2016 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.common.base.Strings;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import java.util.Objects;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcher;
import org.opentcs.strategies.basic.parking.DefaultParkingPositionSupplier;
import org.opentcs.strategies.basic.recharging.DefaultRechargePositionSupplier;
import org.opentcs.strategies.basic.recovery.DefaultRecoveryEvaluator;
import org.opentcs.strategies.basic.routing.DefaultRouter;
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
import org.opentcs.strategies.basic.scheduling.DefaultScheduler;
import org.opentcs.util.configuration.ConfigurationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guice configuration for default kernel strategies.
 * (These can be overridden in custom Guice modules.)
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultKernelStrategiesModule
    extends KernelInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultKernelStrategiesModule.class);

  // tag::documentation_configureDefaultStrategies[]
  @Override
  protected void configure() {
    configureSchedulerDependencies();
    bindScheduler(DefaultScheduler.class);

    configureRouterDependencies();
    bindRouter(DefaultRouter.class);

    configureParkingPositionSupplierDependencies();
    bindParkingPositionSupplier(DefaultParkingPositionSupplier.class);

    configureRechargePositionSupplierDependencies();
    bindRechargePositionSupplier(DefaultRechargePositionSupplier.class);

    configureDispatcherDependencies();
    bindDispatcher(DefaultDispatcher.class);

    configureRecoveryEvaluatorDependencies();
    bindRecoveryEvaluator(DefaultRecoveryEvaluator.class);
  }
  // end::documentation_configureDefaultStrategies[]

  private void configureSchedulerDependencies() {
    Multibinder.newSetBinder(binder(), Scheduler.Module.class);
  }

  private void configureRouterDependencies() {
    ConfigurationStore routerConfig
        = ConfigurationStore.getStore(DefaultRouter.class.getName());

    configureTableBuilder(routerConfig.getString("tableBuilderType", "BFS"));

    bindConstant()
        .annotatedWith(DefaultRouter.RouteToCurrentPos.class)
        .to(routerConfig.getBoolean("routeToCurrentPosition", false));
  }

  private void configureParkingPositionSupplierDependencies() {
  }

  private void configureRechargePositionSupplierDependencies() {
  }

  private void configureDispatcherDependencies() {
    ConfigurationStore configStore
        = ConfigurationStore.getStore(DefaultDispatcher.class.getName());
    bindConstant()
        .annotatedWith(DefaultDispatcher.ParkWhenIdle.class)
        .to(configStore.getBoolean("parkIdleVehicles", false));
    bindConstant()
        .annotatedWith(DefaultDispatcher.RechargeWhenIdle.class)
        .to(configStore.getBoolean("rechargeVehiclesWhenIdle", false));
    bindConstant()
        .annotatedWith(DefaultDispatcher.RechargeWhenEnergyCritical.class)
        .to(configStore.getBoolean("rechargeVehiclesWhenEnergyCritical", false));
  }

  private void configureRecoveryEvaluatorDependencies() {
    ConfigurationStore evalConfigStore
        = ConfigurationStore.getStore(DefaultRecoveryEvaluator.class.getName());
    bindConstant()
        .annotatedWith(DefaultRecoveryEvaluator.Threshold.class)
        .to(evalConfigStore.getDouble("threshold", 0.7));
  }

  @Provides
  RouteEvaluator provideRouteEvaluator() {
    ConfigurationStore routerConfig
        = ConfigurationStore.getStore(DefaultRouter.class.getName());
    // hops, distance, traveltime, turns, explicit (+null)
    String costFactorsString = routerConfig.getString("routingCostFactors",
                                                      "distance, turns");
    String[] costFactors = costFactorsString.trim().toLowerCase().split("[, ]+");
    RouteEvaluator result = new RouteEvaluatorNull();
    for (String costFactor : costFactors) {
      if (Strings.isNullOrEmpty(costFactor)) {
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
          LOG.warn("Illegal cost factor '{}', ignored.", costFactor);
      }
    }
    // Make sure at least one cost factor is used.
    if (result instanceof RouteEvaluatorNull) {
      LOG.warn("No cost factor configured, falling back to distance.");
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
      LOG.warn("Unknown builder type '{}', using BFS", builderType);
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
