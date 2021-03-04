/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import java.util.Comparator;
import javax.inject.Singleton;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcher;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.NoOrderSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;
import org.opentcs.strategies.basic.dispatching.ParkingOrderSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.RechargeOrderSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.ReservedOrderSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.TransportOrderSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.TransportOrderSelector;
import org.opentcs.strategies.basic.dispatching.TransportOrderService;
import org.opentcs.strategies.basic.dispatching.VehicleSelector;
import org.opentcs.strategies.basic.dispatching.parking.DefaultParkingPositionSupplier;
import org.opentcs.strategies.basic.dispatching.recharging.DefaultRechargePositionSupplier;
import org.opentcs.strategies.basic.recovery.DefaultRecoveryEvaluator;
import org.opentcs.strategies.basic.recovery.DefaultRecoveryEvaluatorConfiguration;
import org.opentcs.strategies.basic.routing.DefaultRouter;
import org.opentcs.strategies.basic.routing.DefaultRouterConfiguration;
import org.opentcs.strategies.basic.routing.DefaultRouterConfiguration.TableBuilderType;
import static org.opentcs.strategies.basic.routing.DefaultRouterConfiguration.TableBuilderType.BFS;
import static org.opentcs.strategies.basic.routing.DefaultRouterConfiguration.TableBuilderType.DFS;
import org.opentcs.strategies.basic.routing.RouteEvaluator;
import org.opentcs.strategies.basic.routing.RouteEvaluatorComposite;
import org.opentcs.strategies.basic.routing.RouteEvaluatorDistance;
import org.opentcs.strategies.basic.routing.RouteEvaluatorExplicit;
import org.opentcs.strategies.basic.routing.RouteEvaluatorHops;
import org.opentcs.strategies.basic.routing.RouteEvaluatorTravelTime;
import org.opentcs.strategies.basic.routing.RouteEvaluatorTurns;
import org.opentcs.strategies.basic.routing.RoutingTableBuilder;
import org.opentcs.strategies.basic.routing.RoutingTableBuilderBfs;
import org.opentcs.strategies.basic.routing.RoutingTableBuilderDfs;
import org.opentcs.strategies.basic.scheduling.DefaultScheduler;
import org.opentcs.util.Comparators;
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
    configureRouterDependencies();
    configureDispatcherDependencies();
    configureRecoveryEvaluatorDependencies();
    bindScheduler(DefaultScheduler.class);
    bindRouter(DefaultRouter.class);
    bindDispatcher(DefaultDispatcher.class);
    bindRecoveryEvaluator(DefaultRecoveryEvaluator.class);
  }
  // end::documentation_configureDefaultStrategies[]

  private void configureSchedulerDependencies() {
    Multibinder.newSetBinder(binder(), Scheduler.Module.class);
  }

  private void configureRouterDependencies() {
    DefaultRouterConfiguration configuration
        = getConfigBindingProvider().get(DefaultRouterConfiguration.PREFIX,
                                         DefaultRouterConfiguration.class);
    bind(DefaultRouterConfiguration.class)
        .toInstance(configuration);
    configureTableBuilder(configuration.tableBuilderType());

    bind(RouteEvaluator.class)
        .toProvider(() -> {
          RouteEvaluatorComposite result = new RouteEvaluatorComposite();
          for (DefaultRouterConfiguration.EvaluatorType type : configuration.routeEvaluators()) {
            switch (type) {
              case DISTANCE:
                result.getComponents().add(new RouteEvaluatorDistance());
                break;
              case TRAVELTIME:
                result.getComponents().add(new RouteEvaluatorTravelTime());
                break;
              case HOPS:
                result.getComponents().add(new RouteEvaluatorHops());
                break;
              case TURNS:
                result.getComponents().add(new RouteEvaluatorTurns(configuration.turnCosts()));
                break;
              case EXPLICIT:
                result.getComponents().add(new RouteEvaluatorExplicit());
                break;
              default:
                throw new IllegalArgumentException("Unhandled evaluator type: " + type);
            }
          }
          // Make sure at least one evaluator is used.
          if (result.getComponents().isEmpty()) {
            LOG.warn("No route evaluator enabled, falling back to distance.");
            result.getComponents().add(new RouteEvaluatorDistance());
          }
          return result;
        });
  }

  private void configureTableBuilder(TableBuilderType type) {
    switch (type) {
      case DFS:
        bind(RoutingTableBuilder.class).to(RoutingTableBuilderDfs.class);
        break;
      case BFS:
        bind(RoutingTableBuilder.class).to(RoutingTableBuilderBfs.class);
        break;
      default:
        throw new IllegalArgumentException("No handling for builder type '" + type.name() + "'");
    }
  }

  @SuppressWarnings("deprecation")
  private void configureDispatcherDependencies() {
    bind(DefaultDispatcherConfiguration.class)
        .toInstance(getConfigBindingProvider().get(DefaultDispatcherConfiguration.PREFIX,
                                                   DefaultDispatcherConfiguration.class));

    bind(OrderReservationPool.class)
        .in(Singleton.class);

    bind(org.opentcs.components.kernel.ParkingPositionSupplier.class)
        .to(DefaultParkingPositionSupplier.class)
        .in(Singleton.class);
    bind(org.opentcs.components.kernel.RechargePositionSupplier.class)
        .to(DefaultRechargePositionSupplier.class)
        .in(Singleton.class);

    bind(VehicleSelector.class)
        .in(Singleton.class);

    bind(new TypeLiteral<Comparator<TransportOrder>>() {
    })
        .annotatedWith(TransportOrderSelectionStrategy.OrderComparator.class)
        .toInstance(Comparators.ordersByDeadline());
    bind(NoOrderSelectionStrategy.class)
        .in(Singleton.class);
    bind(ReservedOrderSelectionStrategy.class)
        .in(Singleton.class);
    bind(TransportOrderSelectionStrategy.class)
        .in(Singleton.class);
    bind(RechargeOrderSelectionStrategy.class)
        .in(Singleton.class);
    bind(ParkingOrderSelectionStrategy.class)
        .in(Singleton.class);
    bind(TransportOrderSelector.class)
        .in(Singleton.class);

    bind(TransportOrderService.class)
        .in(Singleton.class);
  }

  private void configureRecoveryEvaluatorDependencies() {
    bind(DefaultRecoveryEvaluatorConfiguration.class)
        .toInstance(getConfigBindingProvider().get(DefaultRecoveryEvaluatorConfiguration.PREFIX,
                                                   DefaultRecoveryEvaluatorConfiguration.class));
  }
}
