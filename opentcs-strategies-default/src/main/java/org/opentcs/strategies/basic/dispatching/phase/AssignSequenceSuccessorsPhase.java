// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.Optional;
import org.opentcs.components.kernel.services.TCSObjectService;
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
  private final TCSObjectService objectService;
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
      TCSObjectService objectService,
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
    for (Vehicle vehicle : objectService.fetchObjects(
        Vehicle.class,
        this::readyForNextInSequence
    )) {
      tryAssignNextOrderInSequence(vehicle);
    }
  }

  private void tryAssignNextOrderInSequence(Vehicle vehicle) {
    nextOrderInCurrentSequence(vehicle)
        .map(
            order -> computeCandidate(
                vehicle,
                objectService.fetchObject(Point.class, vehicle.getCurrentPosition()),
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

  private Optional<TransportOrder> nextOrderInCurrentSequence(Vehicle vehicle) {
    OrderSequence seq = objectService.fetchObject(OrderSequence.class, vehicle.getOrderSequence());

    // If the order sequence's next order is not available, yet, the vehicle should wait for it.
    if (seq.getNextUnfinishedOrder() == null) {
      return Optional.empty();
    }

    // Return the next order to be processed for the sequence.
    return Optional.of(
        objectService.fetchObject(
            TransportOrder.class,
            seq.getNextUnfinishedOrder()
        )
    );
  }

  private boolean readyForNextInSequence(Vehicle vehicle) {
    return vehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED
        && vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && vehicle.hasState(Vehicle.State.IDLE)
        && vehicle.getCurrentPosition() != null
        && vehicle.getOrderSequence() != null;
  }

}
