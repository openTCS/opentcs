/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.services;

import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;

/**
 * Provides methods concerning {@link TransportOrder}s and {@link OrderSequence}s.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface TransportOrderService
    extends TCSObjectService {

  /**
   * Creates a new order sequence.
   * A new order sequence is created with a generated unique ID and all other attributes taken from
   * the given transfer object.
   * A copy of the newly created order sequence is then returned.
   *
   * @param to Describes the order sequence to be created.
   * @return A copy of the newly created order sequence.
   * @throws ObjectUnknownException If any referenced object does not exist.
   * @throws ObjectExistsException If an object with the same name already exists in the model.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  OrderSequence createOrderSequence(OrderSequenceCreationTO to)
      throws ObjectUnknownException, ObjectExistsException, KernelRuntimeException;

  /**
   * Creates a new transport order.
   * A new transport order is created with a generated unique ID and all other attributes taken from
   * the given transfer object.
   * This method also implicitly adds the transport order to its wrapping sequence, if any.
   * A copy of the newly created transport order is then returned.
   *
   * @param to Describes the transport order to be created.
   * @return A copy of the newly created transport order.
   * @throws ObjectUnknownException If any referenced object does not exist.
   * @throws ObjectExistsException If an object with the same name already exists in the model.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  TransportOrder createTransportOrder(TransportOrderCreationTO to)
      throws ObjectUnknownException, ObjectExistsException, KernelRuntimeException;

  /**
   * Marks an order sequence as complete by setting its complete flag.
   *
   * @param ref A reference to the order sequence to be modified.
   * @throws ObjectUnknownException If the referenced order sequence does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void markOrderSequenceComplete(TCSObjectReference<OrderSequence> ref)
      throws ObjectUnknownException, KernelRuntimeException;
}
