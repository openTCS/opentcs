/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import java.util.Comparator;
import javax.inject.Singleton;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcher;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;
import org.opentcs.strategies.basic.dispatching.TransportOrderSelectionVeto;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;
import org.opentcs.strategies.basic.dispatching.phase.parking.DefaultParkingPositionSupplier;
import org.opentcs.strategies.basic.dispatching.phase.recharging.DefaultRechargePositionSupplier;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeOrderCandidateComparator;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeOrderComparator;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeVehicleCandidateComparator;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeVehicleComparator;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorByCompleteRoutingCosts;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorByDeadline;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorByEnergyLevel;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorByInitialRoutingCosts;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorByOrderAge;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorByOrderName;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorByVehicleName;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorIdleFirst;
import org.opentcs.strategies.basic.dispatching.priorization.transportorder.TransportOrderComparatorByAge;
import org.opentcs.strategies.basic.dispatching.priorization.transportorder.TransportOrderComparatorByDeadline;
import org.opentcs.strategies.basic.dispatching.priorization.transportorder.TransportOrderComparatorByName;
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorByEnergyLevel;
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorByName;
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorIdleFirst;
import org.opentcs.strategies.basic.recovery.DefaultRecoveryEvaluatorConfiguration;
import org.opentcs.strategies.basic.routing.DefaultRouter;
import org.opentcs.strategies.basic.routing.DefaultRouterConfiguration;
import org.opentcs.strategies.basic.routing.PointRouterFactory;
import org.opentcs.strategies.basic.routing.jgrapht.BellmanFordPointRouterFactory;
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
    bindScheduler(DefaultScheduler.class);
    bindRouter(DefaultRouter.class);
    bindDispatcher(DefaultDispatcher.class);
    configureRecoveryEvaluator();
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

    MapBinder<String, Comparator<Vehicle>> vehicleComparatorBinder
        = MapBinder.newMapBinder(binder(),
                                 new TypeLiteral<String>() {
                             },
                                 new TypeLiteral<Comparator<Vehicle>>() {
                             });
    vehicleComparatorBinder
        .addBinding(VehicleComparatorByEnergyLevel.CONFIGURATION_KEY)
        .to(VehicleComparatorByEnergyLevel.class);
    vehicleComparatorBinder
        .addBinding(VehicleComparatorByName.CONFIGURATION_KEY)
        .to(VehicleComparatorByName.class);
    vehicleComparatorBinder
        .addBinding(VehicleComparatorIdleFirst.CONFIGURATION_KEY)
        .to(VehicleComparatorIdleFirst.class);

    MapBinder<String, Comparator<TransportOrder>> orderComparatorBinder
        = MapBinder.newMapBinder(binder(),
                                 new TypeLiteral<String>() {
                             },
                                 new TypeLiteral<Comparator<TransportOrder>>() {
                             });
    orderComparatorBinder
        .addBinding(TransportOrderComparatorByAge.CONFIGURATION_KEY)
        .to(TransportOrderComparatorByAge.class);
    orderComparatorBinder
        .addBinding(TransportOrderComparatorByDeadline.CONFIGURATION_KEY)
        .to(TransportOrderComparatorByDeadline.class);
    orderComparatorBinder
        .addBinding(TransportOrderComparatorByName.CONFIGURATION_KEY)
        .to(TransportOrderComparatorByName.class);

    MapBinder<String, Comparator<AssignmentCandidate>> candidateComparatorBinder
        = MapBinder.newMapBinder(binder(),
                                 new TypeLiteral<String>() {
                             },
                                 new TypeLiteral<Comparator<AssignmentCandidate>>() {
                             });
    candidateComparatorBinder
        .addBinding(CandidateComparatorByCompleteRoutingCosts.CONFIGURATION_KEY)
        .to(CandidateComparatorByCompleteRoutingCosts.class);
    candidateComparatorBinder
        .addBinding(CandidateComparatorByDeadline.CONFIGURATION_KEY)
        .to(CandidateComparatorByDeadline.class);
    candidateComparatorBinder
        .addBinding(CandidateComparatorByEnergyLevel.CONFIGURATION_KEY)
        .to(CandidateComparatorByEnergyLevel.class);
    candidateComparatorBinder
        .addBinding(CandidateComparatorByInitialRoutingCosts.CONFIGURATION_KEY)
        .to(CandidateComparatorByInitialRoutingCosts.class);
    candidateComparatorBinder
        .addBinding(CandidateComparatorByOrderAge.CONFIGURATION_KEY)
        .to(CandidateComparatorByOrderAge.class);
    candidateComparatorBinder
        .addBinding(CandidateComparatorByOrderName.CONFIGURATION_KEY)
        .to(CandidateComparatorByOrderName.class);
    candidateComparatorBinder
        .addBinding(CandidateComparatorByVehicleName.CONFIGURATION_KEY)
        .to(CandidateComparatorByVehicleName.class);
    candidateComparatorBinder
        .addBinding(CandidateComparatorIdleFirst.CONFIGURATION_KEY)
        .to(CandidateComparatorIdleFirst.class);

    bind(CompositeVehicleComparator.class)
        .in(Singleton.class);
    bind(CompositeOrderComparator.class)
        .in(Singleton.class);
    bind(CompositeOrderCandidateComparator.class)
        .in(Singleton.class);
    bind(CompositeVehicleCandidateComparator.class)
        .in(Singleton.class);

    bind(TransportOrderUtil.class)
        .in(Singleton.class);
  }

  @SuppressWarnings("deprecation")
  private void configureRecoveryEvaluator() {
    bind(DefaultRecoveryEvaluatorConfiguration.class)
        .toInstance(getConfigBindingProvider().get(DefaultRecoveryEvaluatorConfiguration.PREFIX,
                                                   DefaultRecoveryEvaluatorConfiguration.class));
    bindRecoveryEvaluator(org.opentcs.strategies.basic.recovery.DefaultRecoveryEvaluator.class);
  }
}
