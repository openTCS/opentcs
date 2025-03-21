// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching;

import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import jakarta.inject.Singleton;
import java.util.Comparator;
import org.opentcs.components.kernel.RouteSelector;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.phase.parking.DefaultParkingPositionSupplier;
import org.opentcs.strategies.basic.dispatching.phase.parking.ParkingPositionSupplier;
import org.opentcs.strategies.basic.dispatching.phase.recharging.DefaultRechargePositionSupplier;
import org.opentcs.strategies.basic.dispatching.phase.recharging.RechargePositionSupplier;
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
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorByOrderTypePriority;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorByVehicleName;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorDeadlineAtRiskFirst;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorIdleFirst;
import org.opentcs.strategies.basic.dispatching.priorization.transportorder.TransportOrderComparatorByAge;
import org.opentcs.strategies.basic.dispatching.priorization.transportorder.TransportOrderComparatorByDeadline;
import org.opentcs.strategies.basic.dispatching.priorization.transportorder.TransportOrderComparatorByName;
import org.opentcs.strategies.basic.dispatching.priorization.transportorder.TransportOrderComparatorDeadlineAtRiskFirst;
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorByEnergyLevel;
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorByName;
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorIdleFirst;
import org.opentcs.strategies.basic.dispatching.rerouting.ForcedReroutingStrategy;
import org.opentcs.strategies.basic.dispatching.rerouting.RegularDriveOrderMerger;
import org.opentcs.strategies.basic.dispatching.rerouting.RegularReroutingStrategy;
import org.opentcs.strategies.basic.dispatching.rerouting.ReroutingStrategy;
import org.opentcs.strategies.basic.dispatching.selection.AssignmentCandidateSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.ParkVehicleSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.RechargeVehicleSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.ReparkVehicleSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.TransportOrderSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.VehicleSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.candidates.CompositeAssignmentCandidateSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.candidates.IsProcessable;
import org.opentcs.strategies.basic.dispatching.selection.orders.CompositeTransportOrderSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.orders.ContainsLockedTargetLocations;
import org.opentcs.strategies.basic.dispatching.selection.vehicles.CompositeParkVehicleSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.vehicles.CompositeRechargeVehicleSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.vehicles.CompositeReparkVehicleSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.vehicles.CompositeVehicleSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.vehicles.IsIdleAndDegraded;
import org.opentcs.strategies.basic.dispatching.selection.vehicles.IsParkable;
import org.opentcs.strategies.basic.dispatching.selection.vehicles.IsReparkable;

/**
 * Guice configuration for the default dispatcher.
 */
public class DefaultDispatcherModule
    extends
      KernelInjectionModule {

  /**
   * Creates a new instance.
   */
  public DefaultDispatcherModule() {
  }

  @Override
  protected void configure() {
    configureDispatcherDependencies();
    bindDispatcher(DefaultDispatcher.class);
  }

  private void configureDispatcherDependencies() {
    Multibinder.newSetBinder(binder(), VehicleSelectionFilter.class);
    Multibinder.newSetBinder(binder(), TransportOrderSelectionFilter.class)
        .addBinding().to(ContainsLockedTargetLocations.class);
    Multibinder.newSetBinder(binder(), ParkVehicleSelectionFilter.class)
        .addBinding().to(IsParkable.class);
    Multibinder.newSetBinder(binder(), ReparkVehicleSelectionFilter.class)
        .addBinding().to(IsReparkable.class);
    Multibinder.newSetBinder(binder(), RechargeVehicleSelectionFilter.class)
        .addBinding().to(IsIdleAndDegraded.class);
    Multibinder.newSetBinder(binder(), AssignmentCandidateSelectionFilter.class)
        .addBinding().to(IsProcessable.class);

    bind(CompositeParkVehicleSelectionFilter.class)
        .in(Singleton.class);
    bind(CompositeReparkVehicleSelectionFilter.class)
        .in(Singleton.class);
    bind(CompositeRechargeVehicleSelectionFilter.class)
        .in(Singleton.class);
    bind(CompositeTransportOrderSelectionFilter.class)
        .in(Singleton.class);
    bind(CompositeVehicleSelectionFilter.class)
        .in(Singleton.class);
    bind(CompositeAssignmentCandidateSelectionFilter.class)
        .in(Singleton.class);

    bind(DefaultDispatcherConfiguration.class)
        .toInstance(
            getConfigBindingProvider().get(
                DefaultDispatcherConfiguration.PREFIX,
                DefaultDispatcherConfiguration.class
            )
        );

    bind(OrderReservationPool.class)
        .in(Singleton.class);

    bind(ParkingPositionSupplier.class)
        .to(DefaultParkingPositionSupplier.class)
        .in(Singleton.class);
    bind(RechargePositionSupplier.class)
        .to(DefaultRechargePositionSupplier.class)
        .in(Singleton.class);

    MapBinder<String, Comparator<Vehicle>> vehicleComparatorBinder
        = MapBinder.newMapBinder(
            binder(),
            new TypeLiteral<String>() {
            },
            new TypeLiteral<Comparator<Vehicle>>() {
            }
        );
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
        = MapBinder.newMapBinder(
            binder(),
            new TypeLiteral<String>() {
            },
            new TypeLiteral<Comparator<TransportOrder>>() {
            }
        );
    orderComparatorBinder
        .addBinding(TransportOrderComparatorByAge.CONFIGURATION_KEY)
        .to(TransportOrderComparatorByAge.class);
    orderComparatorBinder
        .addBinding(TransportOrderComparatorByDeadline.CONFIGURATION_KEY)
        .to(TransportOrderComparatorByDeadline.class);
    orderComparatorBinder
        .addBinding(TransportOrderComparatorDeadlineAtRiskFirst.CONFIGURATION_KEY)
        .to(TransportOrderComparatorDeadlineAtRiskFirst.class);
    orderComparatorBinder
        .addBinding(TransportOrderComparatorByName.CONFIGURATION_KEY)
        .to(TransportOrderComparatorByName.class);

    MapBinder<String, Comparator<AssignmentCandidate>> candidateComparatorBinder
        = MapBinder.newMapBinder(
            binder(),
            new TypeLiteral<String>() {
            },
            new TypeLiteral<Comparator<AssignmentCandidate>>() {
            }
        );
    candidateComparatorBinder
        .addBinding(CandidateComparatorByCompleteRoutingCosts.CONFIGURATION_KEY)
        .to(CandidateComparatorByCompleteRoutingCosts.class);
    candidateComparatorBinder
        .addBinding(CandidateComparatorByDeadline.CONFIGURATION_KEY)
        .to(CandidateComparatorByDeadline.class);
    candidateComparatorBinder
        .addBinding(CandidateComparatorDeadlineAtRiskFirst.CONFIGURATION_KEY)
        .to(CandidateComparatorDeadlineAtRiskFirst.class);
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
    candidateComparatorBinder
        .addBinding(CandidateComparatorByOrderTypePriority.CONFIGURATION_KEY)
        .to(CandidateComparatorByOrderTypePriority.class);

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

    bind(RouteSelector.class)
        .to(LowestCostRouteSelector.class)
        .in(Singleton.class);

    configureRerouteComponents();
  }

  private void configureRerouteComponents() {
    bind(RerouteUtil.class).in(Singleton.class);
    bind(RegularReroutingStrategy.class).in(Singleton.class);
    bind(RegularDriveOrderMerger.class).in(Singleton.class);

    MapBinder<ReroutingType, ReroutingStrategy> reroutingStrategies
        = MapBinder.newMapBinder(
            binder(),
            ReroutingType.class,
            ReroutingStrategy.class
        );
    reroutingStrategies
        .addBinding(ReroutingType.REGULAR)
        .to(RegularReroutingStrategy.class);
    reroutingStrategies
        .addBinding(ReroutingType.FORCED)
        .to(ForcedReroutingStrategy.class);
  }
}
