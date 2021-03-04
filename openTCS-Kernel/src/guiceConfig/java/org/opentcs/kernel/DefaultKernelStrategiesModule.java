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
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;
import org.opentcs.strategies.basic.dispatching.TransportOrderSelectionVeto;
import org.opentcs.strategies.basic.dispatching.TransportOrderSelector;
import org.opentcs.strategies.basic.dispatching.TransportOrderService;
import org.opentcs.strategies.basic.dispatching.VehicleSelector;
import org.opentcs.strategies.basic.dispatching.orderselection.NoOrderSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.orderselection.ParkingOrderSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.orderselection.RechargeOrderSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.orderselection.ReservedOrderSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.orderselection.TransportOrderSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.orderselection.parking.DefaultParkingPositionSupplier;
import org.opentcs.strategies.basic.dispatching.orderselection.recharging.DefaultRechargePositionSupplier;
import org.opentcs.strategies.basic.dispatching.vehicleselection.AssignedVehicleSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.vehicleselection.AvailableVehicleSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.vehicleselection.ClosestVehicleComparator;
import org.opentcs.strategies.basic.dispatching.vehicleselection.VehicleCandidate;
import org.opentcs.strategies.basic.recovery.DefaultRecoveryEvaluator;
import org.opentcs.strategies.basic.recovery.DefaultRecoveryEvaluatorConfiguration;
import org.opentcs.strategies.basic.routing.DefaultRouter;
import org.opentcs.strategies.basic.routing.DefaultRouterConfiguration;
import org.opentcs.strategies.basic.routing.PointRouterFactory;
import org.opentcs.strategies.basic.routing.jgrapht.DefaultModelGraphMapper;
import org.opentcs.strategies.basic.routing.jgrapht.DijkstraPointRouterFactory;
import org.opentcs.strategies.basic.routing.jgrapht.EdgeEvaluator;
import org.opentcs.strategies.basic.routing.jgrapht.EdgeEvaluatorComposite;
import org.opentcs.strategies.basic.routing.jgrapht.EdgeEvaluatorDistance;
import org.opentcs.strategies.basic.routing.jgrapht.EdgeEvaluatorExplicit;
import org.opentcs.strategies.basic.routing.jgrapht.EdgeEvaluatorHops;
import org.opentcs.strategies.basic.routing.jgrapht.EdgeEvaluatorTravelTime;
import org.opentcs.strategies.basic.routing.jgrapht.FloydWarshallPointRouterFactory;
import org.opentcs.strategies.basic.routing.jgrapht.ModelGraphMapper;
import org.opentcs.strategies.basic.routing.jgrapht.ShortestPathConfiguration;
import static org.opentcs.strategies.basic.routing.jgrapht.ShortestPathConfiguration.EvaluatorType.EXPLICIT;
import static org.opentcs.strategies.basic.routing.jgrapht.ShortestPathConfiguration.EvaluatorType.TRAVELTIME;
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
            switch (type) {
              case DISTANCE:
                result.getComponents().add(new EdgeEvaluatorDistance());
                break;
              case TRAVELTIME:
                result.getComponents().add(new EdgeEvaluatorTravelTime());
                break;
              case HOPS:
                result.getComponents().add(new EdgeEvaluatorHops());
                break;
              case EXPLICIT:
                result.getComponents().add(new EdgeEvaluatorExplicit());
                break;
              default:
                throw new IllegalArgumentException("Unhandled evaluator type: " + type);
            }
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
  private void configureDispatcherDependencies() {
    Multibinder.newSetBinder(binder(), TransportOrderSelectionVeto.class);

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

    bind(new TypeLiteral<Comparator<VehicleCandidate>>() {
    })
        .annotatedWith(AvailableVehicleSelectionStrategy.VehicleCandidateComparator.class)
        .toInstance(new ClosestVehicleComparator());
    bind(AssignedVehicleSelectionStrategy.class)
        .in(Singleton.class);
    bind(AvailableVehicleSelectionStrategy.class)
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
