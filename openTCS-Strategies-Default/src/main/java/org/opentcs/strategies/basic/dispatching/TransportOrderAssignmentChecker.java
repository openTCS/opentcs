/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.dipatching.TransportOrderAssignmentVeto;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;

/**
 * Provides methods to check if the assignment of a {@link TransportOrder} to a {@link Vehicle} is
 * possible.
 */
public class TransportOrderAssignmentChecker {

  private final TCSObjectService objectService;
  private final OrderReservationPool orderReservationPool;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service to use.
   * @param orderReservationPool The pool of order reservations.
   */
  @Inject
  public TransportOrderAssignmentChecker(TCSObjectService objectService,
                                         OrderReservationPool orderReservationPool) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
  }

  /**
   * Checks whether the assignment of the given transport order to its
   * {@link TransportOrder#getIntendedVehicle() intented vehicle} is possible.
   *
   * @param transportOrder The transport order to check.
   * @return A {@link TransportOrderAssignmentVeto} indicating whether the assignment of the given
   * transport order is possible or not.
   */
  public TransportOrderAssignmentVeto checkTransportOrderAssignment(TransportOrder transportOrder) {
    if (!transportOrder.hasState(TransportOrder.State.DISPATCHABLE)) {
      return TransportOrderAssignmentVeto.TRANSPORT_ORDER_STATE_INVALID;
    }

    if (transportOrder.getWrappingSequence() != null) {
      return TransportOrderAssignmentVeto.TRANSPORT_ORDER_PART_OF_ORDER_SEQUENCE;
    }

    if (transportOrder.getIntendedVehicle() == null) {
      return TransportOrderAssignmentVeto.TRANSPORT_ORDER_INTENDED_VEHICLE_NOT_SET;
    }

    Vehicle intendedVehicle = objectService.fetchObject(Vehicle.class,
                                                        transportOrder.getIntendedVehicle());
    if (!intendedVehicle.hasProcState(Vehicle.ProcState.IDLE)) {
      return TransportOrderAssignmentVeto.VEHICLE_PROCESSING_STATE_INVALID;
    }

    if (!intendedVehicle.hasState(Vehicle.State.IDLE)
        && !intendedVehicle.hasState(Vehicle.State.CHARGING)) {
      return TransportOrderAssignmentVeto.VEHICLE_STATE_INVALID;
    }

    if (intendedVehicle.getIntegrationLevel() != Vehicle.IntegrationLevel.TO_BE_UTILIZED) {
      return TransportOrderAssignmentVeto.VEHICLE_INTEGRATION_LEVEL_INVALID;
    }

    if (intendedVehicle.getCurrentPosition() == null) {
      return TransportOrderAssignmentVeto.VEHICLE_CURRENT_POSITION_UNKNOWN;
    }

    if (intendedVehicle.getOrderSequence() != null) {
      return TransportOrderAssignmentVeto.VEHICLE_PROCESSING_ORDER_SEQUENCE;
    }

    if (!orderReservationPool.findReservations(intendedVehicle.getReference()).isEmpty()) {
      return TransportOrderAssignmentVeto.GENERIC_VETO;
    }

    return TransportOrderAssignmentVeto.NO_VETO;
  }
}
