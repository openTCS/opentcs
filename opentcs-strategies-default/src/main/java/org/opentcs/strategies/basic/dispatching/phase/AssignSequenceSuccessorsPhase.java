// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import org.opentcs.components.kernel.services.InternalTCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.DriveOrderRouteAssigner;
import org.opentcs.strategies.basic.dispatching.Phase;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;
import org.opentcs.strategies.basic.dispatching.selection.candidates.CompositeAssignmentCandidateSelectionFilter;

/**
 * Assigns vehicles to the next transport orders in their respective order sequences, if any.
 */
public class AssignSequenceSuccessorsPhase
    implements
      Phase {

  /**
   * The object service
   */
  private final InternalTCSObjectService objectService;
  /**
   * A collection of predicates for filtering assignment candidates.
   */
  private final CompositeAssignmentCandidateSelectionFilter assignmentCandidateSelectionFilter;

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
  public AssignSequenceSuccessorsPhase(
      InternalTCSObjectService objectService,
      CompositeAssignmentCandidateSelectionFilter assignmentCandidateSelectionFilter,
      TransportOrderUtil transportOrderUtil,
      DriveOrderRouteAssigner driveOrderRouteAssigner
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.assignmentCandidateSelectionFilter = requireNonNull(
        assignmentCandidateSelectionFilter,
        "assignmentCandidateSelectionFilter"
    );
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
    objectService.fetch(Vehicle.class).stream()
        .filter(
            vehicle -> isFullyIntegratedWithOrderSequence(vehicle)
                && (readyForNextInSequence(vehicle)
                    || processingSkippableOrderInSequence(vehicle))
        )
        .forEach(this::tryAssignNextOrderInSequence);
  }

  private void tryAssignNextOrderInSequence(Vehicle vehicle) {
    transportOrderUtil.nextDispatchableOrderInSequence(vehicle.getOrderSequence())
        .map(
            order -> computeCandidate(
                vehicle,
                objectService.fetch(Point.class, vehicle.getCurrentPosition()).orElseThrow(),
                order
            )
        )
        .filter(candidate -> assignmentCandidateSelectionFilter.apply(candidate).isEmpty())
        .ifPresent(
            candidate -> transportOrderUtil.assignTransportOrder(
                vehicle,
                candidate.getTransportOrder(),
                candidate.getDriveOrders()
            )
        );
  }

  private AssignmentCandidate computeCandidate(
      Vehicle vehicle,
      Point vehiclePosition,
      TransportOrder order
  ) {
    return driveOrderRouteAssigner.tryAssignRoutes(order, vehicle, vehiclePosition)
        .map(driveOrders -> new AssignmentCandidate(vehicle, order, driveOrders))
        .orElse(null);
  }

  private boolean readyForNextInSequence(Vehicle vehicle) {
    return vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && (vehicle.hasState(Vehicle.State.IDLE) || vehicle.hasState(Vehicle.State.CHARGING));
  }

  private boolean processingSkippableOrderInSequence(Vehicle vehicle) {
    if (vehicle.hasProcState(Vehicle.ProcState.PROCESSING_ORDER)
        && (vehicle.hasState(Vehicle.State.IDLE)
            || vehicle.hasState(Vehicle.State.EXECUTING)
            || vehicle.hasState(Vehicle.State.CHARGING))) {
      TransportOrder currentOrder
          = objectService.fetch(TransportOrder.class, vehicle.getTransportOrder()).orElseThrow();
      if (!currentOrder.isDispensable()) {
        return false;
      }
      OrderSequence seq
          = objectService.fetch(OrderSequence.class, vehicle.getOrderSequence()).orElseThrow();
      return !currentOrder.getReference().equals(seq.getOrders().getLast());
    }
    else {
      return false;
    }
  }

  private boolean isFullyIntegratedWithOrderSequence(Vehicle vehicle) {
    return vehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED
        && vehicle.getCurrentPosition() != null
        && vehicle.getOrderSequence() != null;
  }

}
