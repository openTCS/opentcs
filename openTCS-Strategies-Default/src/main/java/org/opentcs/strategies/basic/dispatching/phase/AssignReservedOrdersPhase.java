/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase;

import static java.util.Objects.requireNonNull;
import java.util.Optional;
import javax.inject.Inject;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;
import org.opentcs.strategies.basic.dispatching.Phase;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;

/**
 * Assigns reserved transport orders (if any) to vehicles that have just finished their withdrawn
 * ones.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AssignReservedOrdersPhase
    implements Phase {

  /**
   * The object service
   */
  private final TCSObjectService objectService;
  /**
   * The Router instance calculating route costs.
   */
  private final Router router;
  /**
   * Checks processability of transport orders for vehicles.
   */
  private final ProcessabilityChecker processabilityChecker;
  /**
   * Stores reservations of orders for vehicles.
   */
  private final OrderReservationPool orderReservationPool;

  private final TransportOrderUtil transportOrderUtil;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public AssignReservedOrdersPhase(TCSObjectService objectService,
                                   Router router,
                                   ProcessabilityChecker processabilityChecker,
                                   OrderReservationPool orderReservationPool,
                                   TransportOrderUtil transportOrderUtil) {
    this.router = requireNonNull(router, "router");
    this.objectService = requireNonNull(objectService, "objectService");
    this.processabilityChecker = requireNonNull(processabilityChecker, "processabilityChecker");
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
    this.transportOrderUtil = requireNonNull(transportOrderUtil, "transportOrderUtil");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }
    initialized = false;
  }

  @Override
  public void run() {
    for (Vehicle vehicle : objectService.fetchObjects(Vehicle.class, this::available)) {
      checkForReservedOrder(vehicle);
    }
  }

  private void checkForReservedOrder(Vehicle vehicle) {
    // Check if there's an order reserved for this vehicle that is in an assignable state. If yes,
    // assign that.
    // Note that we expect no more than a single reserved order, and remove ALL reservations if we
    // find at least one, even if it cannot be processed by the vehicle in the end.
    orderReservationPool.findReservations(vehicle.getReference()).stream()
        .map(orderRef -> objectService.fetchObject(TransportOrder.class, orderRef))
        .filter(order -> order.hasState(TransportOrder.State.DISPATCHABLE))
        .limit(1)
        .peek(order -> orderReservationPool.removeReservations(vehicle.getReference()))
        .filter(order -> processabilityChecker.checkProcessability(vehicle, order))
        .findFirst()
        .map(order -> computeRoute(vehicle, order))
        .filter(optSelection -> optSelection.isPresent())
        .map(optSelection -> optSelection.get())
        .ifPresent(
            selection -> transportOrderUtil.assignTransportOrder(vehicle,
                                                                 selection.getTransportOrder(),
                                                                 selection.getDriveOrders())
        );
  }

  private boolean available(Vehicle vehicle) {
    return vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && (vehicle.hasState(Vehicle.State.IDLE)
            || vehicle.hasState(Vehicle.State.CHARGING) && vehicle.isEnergyLevelGood());
  }

  private Optional<VehicleOrderSelection> computeRoute(Vehicle vehicle, TransportOrder order) {
    return router.getRoute(vehicle,
                           objectService.fetchObject(Point.class,
                                                     vehicle.getCurrentPosition()),
                           order)
        .map(driveOrders -> new VehicleOrderSelection(order, vehicle, driveOrders));
  }

}
