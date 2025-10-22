// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.Objects;
import java.util.Optional;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.DriveOrderRouteAssigner;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;
import org.opentcs.strategies.basic.dispatching.Phase;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;
import org.opentcs.strategies.basic.dispatching.selection.candidates.CompositeAssignmentCandidateSelectionFilter;

/**
 * Assigns reserved transport orders (if any) to vehicles that have just finished their withdrawn
 * ones.
 */
public class AssignReservedOrdersPhase
    implements
      Phase {

  /**
   * The object service
   */
  private final TCSObjectService objectService;
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
   * Assigns routes to drive orders.
   */
  private final DriveOrderRouteAssigner driveOrderRouteAssigner;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public AssignReservedOrdersPhase(
      TCSObjectService objectService,
      CompositeAssignmentCandidateSelectionFilter assignmentCandidateSelectionFilter,
      OrderReservationPool orderReservationPool,
      TransportOrderUtil transportOrderUtil,
      DriveOrderRouteAssigner driveOrderRouteAssigner
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.assignmentCandidateSelectionFilter = requireNonNull(
        assignmentCandidateSelectionFilter,
        "assignmentCandidateSelectionFilter"
    );
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
    this.transportOrderUtil = requireNonNull(transportOrderUtil, "transportOrderUtil");
    this.driveOrderRouteAssigner = requireNonNull(
        driveOrderRouteAssigner,
        "driveOrderRouteAssigner"
    );
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
    for (Vehicle vehicle : objectService.fetch(Vehicle.class)) {
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
        .map(orderRef -> objectService.fetch(TransportOrder.class, orderRef).orElseThrow())
        .filter(order -> order.hasState(TransportOrder.State.DISPATCHABLE))
        // A transport order's intended vehicle can change after its creation and also after
        // reservation. Only handle orders where the intended vehicle (still) fits the reservation.
        .filter(order -> hasNoOrMatchingIntendedVehicle(order, vehicle))
        .limit(1)
        .map(
            order -> computeCandidate(
                vehicle,
                objectService.fetch(Point.class, vehicle.getCurrentPosition()).orElseThrow(),
                order
            )
        )
        .filter(optCandidate -> optCandidate.isPresent())
        .map(optCandidate -> optCandidate.get())
        .filter(candidate -> assignmentCandidateSelectionFilter.apply(candidate).isEmpty())
        .findFirst()
        .ifPresent(
            candidate -> transportOrderUtil.assignTransportOrder(
                vehicle,
                candidate.getTransportOrder(),
                candidate.getDriveOrders()
            )
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

  private Optional<AssignmentCandidate> computeCandidate(
      Vehicle vehicle,
      Point vehiclePosition,
      TransportOrder order
  ) {
    return driveOrderRouteAssigner.tryAssignRoutes(order, vehicle, vehiclePosition)
        .map(driveOrders -> new AssignmentCandidate(vehicle, order, driveOrders));
  }
}
