/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.dipatching;

import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;

/**
 * Defines reasons for a transport order assignment not being possible.
 */
public enum TransportOrderAssignmentVeto {

  /**
   * There is no reason that prevents the transport order assignment.
   */
  NO_VETO,
  /**
   * The transport order's {@link TransportOrder.State} is invalid (e.g. because it's not in state
   * {@link TransportOrder.State#DISPATCHABLE}).
   */
  TRANSPORT_ORDER_STATE_INVALID,
  /**
   * The transport order is part of an {@link OrderSequence}.
   */
  TRANSPORT_ORDER_PART_OF_ORDER_SEQUENCE,
  /**
   * The transport order has its intended vehicle not set.
   */
  TRANSPORT_ORDER_INTENDED_VEHICLE_NOT_SET,
  /**
   * The {@link Vehicle.ProcState} of the vehicle to assign the transport order to is invalid (e.g.
   * because it's not {@link Vehicle.ProcState#IDLE}).
   */
  VEHICLE_PROCESSING_STATE_INVALID,
  /**
   * The {@link Vehicle.State} of the vehicle to assign the transport order to is invalid (e.g.
   * because it's neither {@link Vehicle.State#IDLE} nor {@link Vehicle.State#CHARGING}).
   */
  VEHICLE_STATE_INVALID,
  /**
   * The {@link Vehicle.IntegrationLevel} of the vehicle to assign the transport order to is invalid
   * (e.g. because it's not {@link Vehicle.IntegrationLevel#TO_BE_UTILIZED}).
   */
  VEHICLE_INTEGRATION_LEVEL_INVALID,
  /**
   * The current position of the vehicle to assign the transport order to is unknown.
   */
  VEHICLE_CURRENT_POSITION_UNKNOWN,
  /**
   * The vehicle to assign the transport order to is processing an {@link OrderSequence}.
   */
  VEHICLE_PROCESSING_ORDER_SEQUENCE,
  /**
   * A generic (dispatcher implementation-specific) reason that prevents the transport order
   * assignment.
   */
  GENERIC_VETO;
}
