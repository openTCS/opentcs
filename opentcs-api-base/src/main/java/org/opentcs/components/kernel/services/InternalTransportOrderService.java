// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel.services;

import java.util.List;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;

/**
 * Declares the methods the transport order service must provide which are not accessible to remote
 * peers.
 */
public interface InternalTransportOrderService
    extends
      TransportOrderService {

  /**
   * Sets an order sequence's finished flag.
   *
   * @param ref A reference to the order sequence to be modified.
   * @throws ObjectUnknownException If the referenced transport order is not in this pool.
   */
  void markOrderSequenceFinished(TCSObjectReference<OrderSequence> ref)
      throws ObjectUnknownException;

  /**
   * Updates an order sequence's finished index.
   *
   * @param ref A reference to the order sequence to be modified.
   * @param index The sequence's new finished index.
   * @throws ObjectUnknownException If the referenced transport order is not in this pool.
   */
  void updateOrderSequenceFinishedIndex(TCSObjectReference<OrderSequence> ref, int index)
      throws ObjectUnknownException;

  /**
   * Updates an order sequence's processing vehicle.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param vehicleRef A reference to the vehicle processing the order sequence.
   * @throws ObjectUnknownException If the referenced transport order is not in this pool.
   */
  void updateOrderSequenceProcessingVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef
  )
      throws ObjectUnknownException;

  /**
   * Updates a transport order's list of drive orders.
   *
   * @param ref A reference to the transport order to be modified.
   * @param driveOrders The drive orders containing the data to be copied into this transport
   * order's drive orders.
   * @throws ObjectUnknownException If the referenced transport order does not exist.
   */
  void updateTransportOrderDriveOrders(
      TCSObjectReference<TransportOrder> ref,
      List<DriveOrder> driveOrders
  )
      throws ObjectUnknownException;

  /**
   * Updates a transport order's current drive order.
   * Marks the current drive order as finished, adds it to the list of past drive orders and sets
   * the current drive order to the next one of the list of future drive orders (or {@code null},
   * if that list is empty).
   * Also implicitly sets the {@link TransportOrder#getCurrentRouteStepIndex() transport order's
   * current route step index} to {@link TransportOrder#ROUTE_STEP_INDEX_DEFAULT}.
   * If the current drive order is {@code null} because all drive orders have been finished
   * already or none has been started, yet, nothing happens.
   *
   * @param ref A reference to the transport order to be modified.
   * @throws ObjectUnknownException If the referenced transport order is not in this pool.
   */
  void updateTransportOrderNextDriveOrder(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException;

  /**
   * Updates a transport order's index of the last route step travelled for the currently processed
   * drive order.
   *
   * @param ref A reference to the transport order to be modified.
   * @param index The new index.
   * @throws ObjectUnknownException If the referenced transport order does not exist.
   */
  void updateTransportOrderCurrentRouteStepIndex(
      TCSObjectReference<TransportOrder> ref,
      int index
  )
      throws ObjectUnknownException;

  /**
   * Updates a transport order's processing vehicle.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param vehicleRef A reference to the vehicle processing the order.
   * @param driveOrders The drive orders containing the data to be copied into this transport
   * order's drive orders.
   * @throws ObjectUnknownException If the referenced transport order does not exist.
   * @throws IllegalArgumentException If the destinations of the given drive orders do not match
   * the destinations of the drive orders in this transport order.
   */
  void updateTransportOrderProcessingVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef,
      List<DriveOrder> driveOrders
  )
      throws ObjectUnknownException,
        IllegalArgumentException;

  /**
   * Updates a transport order's state.
   * Note that transport order states are intended to be manipulated by the dispatcher only.
   * Calling this method from any other parts of the kernel may result in undefined behaviour.
   *
   * @param ref A reference to the transport order to be modified.
   * @param state The transport order's new state.
   * @throws ObjectUnknownException If the referenced transport order does not exist.
   */
  void updateTransportOrderState(
      TCSObjectReference<TransportOrder> ref,
      TransportOrder.State state
  )
      throws ObjectUnknownException;
}
