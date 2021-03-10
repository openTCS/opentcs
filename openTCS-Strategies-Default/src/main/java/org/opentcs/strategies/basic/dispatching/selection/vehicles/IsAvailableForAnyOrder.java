/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.selection.vehicles;

import static java.util.Objects.requireNonNull;
import java.util.function.Predicate;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;
import org.opentcs.strategies.basic.dispatching.selection.VehicleSelectionFilter;

/**
 * Filters vehicles that are generally available for transport orders.
 *
 * <p>
 * Note: This filter is not a {@link VehicleSelectionFilter} by intention, since it is not
 * intended to be used in contexts where {@link ObjectHistory} entries are created.
 * </p>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class IsAvailableForAnyOrder
    implements Predicate<Vehicle> {

  /**
   * The object service.
   */
  private final TCSObjectService objectService;
  /**
   * Stores reservations of orders for vehicles.
   */
  private final OrderReservationPool orderReservationPool;
  /**
   * The default dispatcher configuration.
   */
  private final DefaultDispatcherConfiguration configuration;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service.
   * @param orderReservationPool Stores reservations of orders for vehicles.
   * @param configuration The default dispatcher configuration.
   */
  @Inject
  public IsAvailableForAnyOrder(TCSObjectService objectService,
                                OrderReservationPool orderReservationPool,
                                DefaultDispatcherConfiguration configuration) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public boolean test(Vehicle vehicle) {
    return vehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED
        && vehicle.getCurrentPosition() != null
        && vehicle.getOrderSequence() == null
        && !vehicle.isEnergyLevelCritical()
        && !needsMoreCharging(vehicle)
        && (processesNoOrder(vehicle)
            || processesDispensableOrder(vehicle))
        && !hasOrderReservation(vehicle);
  }

  private boolean needsMoreCharging(Vehicle vehicle) {
    return vehicle.hasState(Vehicle.State.CHARGING)
        && !rechargeThresholdReached(vehicle);
  }

  private boolean rechargeThresholdReached(Vehicle vehicle) {
    return configuration.keepRechargingUntilFullyCharged()
        ? vehicle.isEnergyLevelFullyRecharged()
        : vehicle.isEnergyLevelSufficientlyRecharged();
  }

  private boolean processesNoOrder(Vehicle vehicle) {
    return vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && (vehicle.hasState(Vehicle.State.IDLE)
            || vehicle.hasState(Vehicle.State.CHARGING));
  }

  private boolean processesDispensableOrder(Vehicle vehicle) {
    return vehicle.hasProcState(Vehicle.ProcState.PROCESSING_ORDER)
        && objectService.fetchObject(TransportOrder.class, vehicle.getTransportOrder())
            .isDispensable();
  }

  private boolean hasOrderReservation(Vehicle vehicle) {
    return !orderReservationPool.findReservations(vehicle.getReference()).isEmpty();
  }
}
