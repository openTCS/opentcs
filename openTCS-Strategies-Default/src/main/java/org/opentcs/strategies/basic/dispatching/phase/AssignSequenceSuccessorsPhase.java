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
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.Phase;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;

/**
 * Assigns vehicles to the next transport orders in their respective order sequences, if any.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AssignSequenceSuccessorsPhase
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

  private final TransportOrderUtil transportOrderUtil;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public AssignSequenceSuccessorsPhase(TCSObjectService objectService,
                                       Router router,
                                       ProcessabilityChecker processabilityChecker,
                                       TransportOrderUtil transportOrderUtil) {
    this.router = requireNonNull(router, "router");
    this.objectService = requireNonNull(objectService, "objectService");
    this.processabilityChecker = requireNonNull(processabilityChecker, "processabilityChecker");
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
    for (Vehicle vehicle : objectService.fetchObjects(Vehicle.class,
                                                      this::readyForNextInSequence)) {
      tryAssignNextOrderInSequence(vehicle);
    }
  }

  private void tryAssignNextOrderInSequence(Vehicle vehicle) {
    nextOrderInCurrentSequence(vehicle)
        .map(order -> computeCandidate(vehicle, order))
        .filter(candidate -> processabilityChecker.checkProcessability(vehicle,
                                                                       candidate.getTransportOrder()))
        .ifPresent(candidate -> transportOrderUtil.assignTransportOrder(vehicle,
                                                                        candidate.getTransportOrder(),
                                                                        candidate.getDriveOrders()));
  }

  private AssignmentCandidate computeCandidate(Vehicle vehicle, TransportOrder order) {
    return router.getRoute(vehicle,
                           objectService.fetchObject(Point.class, vehicle.getCurrentPosition()),
                           order)
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
    return Optional.of(objectService.fetchObject(TransportOrder.class,
                                                 seq.getNextUnfinishedOrder()));
  }

  private boolean readyForNextInSequence(Vehicle vehicle) {
    return vehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED
        && vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && vehicle.hasState(Vehicle.State.IDLE)
        && vehicle.getCurrentPosition() != null
        && vehicle.getOrderSequence() != null;
  }

}
