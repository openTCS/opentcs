/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase;

import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import javax.inject.Inject;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;
import org.opentcs.strategies.basic.dispatching.Phase;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;
import org.opentcs.strategies.basic.dispatching.selection.candidates.CompositeAssignmentCandidateSelectionFilter;

/**
 * Assigns reserved transport orders (if any) to vehicles that have just finished their withdrawn
 * ones.
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
   * A collection of predicates for filtering assignment candidates.
   */
  private final CompositeAssignmentCandidateSelectionFilter assignmentCandidateSelectionFilter;
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
  public AssignReservedOrdersPhase(
      TCSObjectService objectService,
      Router router,
      CompositeAssignmentCandidateSelectionFilter assignmentCandidateSelectionFilter,
      OrderReservationPool orderReservationPool,
      TransportOrderUtil transportOrderUtil) {
    this.router = requireNonNull(router, "router");
    this.objectService = requireNonNull(objectService, "objectService");
    this.assignmentCandidateSelectionFilter = requireNonNull(assignmentCandidateSelectionFilter,
                                                             "assignmentCandidateSelectionFilter");
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
    for (Vehicle vehicle : objectService.fetchObjects(Vehicle.class)) {
      if (availableForReservedOrders(vehicle)) {
        checkForReservedOrder(vehicle);
      }
      else if (unusableForReservedOrders(vehicle)) {
        orderReservationPool.removeReservations(vehicle.getReference());
      }
    }
  }

  private void checkForReservedOrder(Vehicle vehicle) {
    // Check if there's an order reserved for this vehicle that is in an assignable state. If yes,
    // try to assign that.
    // Note that we expect no more than a single reserved order, and remove ALL reservations if we
    // find at least one, even if it cannot be processed by the vehicle in the end.
    orderReservationPool.findReservations(vehicle.getReference()).stream()
        .map(orderRef -> objectService.fetchObject(TransportOrder.class, orderRef))
        .filter(order -> order.hasState(TransportOrder.State.DISPATCHABLE))
        // A transport order's intended vehicle can change after its creation and also after
        // reservation. Only handle orders where the intended vehicle (still) fits the reservation.
        .filter(order -> hasNoOrMatchingIntendedVehicle(order, vehicle))
        .limit(1)
        .map(order -> computeCandidate(vehicle,
                                       objectService.fetchObject(Point.class,
                                                                 vehicle.getCurrentPosition()),
                                       order))
        .filter(optCandidate -> optCandidate.isPresent())
        .map(optCandidate -> optCandidate.get())
        .filter(candidate -> assignmentCandidateSelectionFilter.apply(candidate).isEmpty())
        .findFirst()
        .ifPresent(
            candidate -> transportOrderUtil.assignTransportOrder(vehicle,
                                                                 candidate.getTransportOrder(),
                                                                 candidate.getDriveOrders())
        );

    // Regardless of whether a reserved order could be assigned to the vehicle or not, remove any
    // reservations for the vehicle and allow it to be reserved (again) in the subsequent dispatcher
    // phases.
    orderReservationPool.removeReservations(vehicle.getReference());
  }

  private boolean availableForReservedOrders(Vehicle vehicle) {
    return vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && (vehicle.hasState(Vehicle.State.IDLE)
            || vehicle.hasState(Vehicle.State.CHARGING))
        && vehicle.getCurrentPosition() != null
        && vehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED;
  }

  private boolean unusableForReservedOrders(Vehicle vehicle) {
    return vehicle.getIntegrationLevel() != Vehicle.IntegrationLevel.TO_BE_UTILIZED;
  }

  private boolean hasNoOrMatchingIntendedVehicle(TransportOrder order, Vehicle vehicle) {
    return order.getIntendedVehicle() == null
        || Objects.equals(order.getIntendedVehicle(), vehicle.getReference());
  }

  private Optional<AssignmentCandidate> computeCandidate(Vehicle vehicle,
                                                         Point vehiclePosition,
                                                         TransportOrder order) {
    return router.getRoute(vehicle, vehiclePosition, order)
        .map(driveOrders -> new AssignmentCandidate(vehicle, order, driveOrders));
  }
}
