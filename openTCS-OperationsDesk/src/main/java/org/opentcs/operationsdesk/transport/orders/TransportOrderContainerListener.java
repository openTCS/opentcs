/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.transport.orders;

import java.util.Collection;
import org.opentcs.data.order.TransportOrder;

/**
 * Listener for changes in the {@link TransportOrdersContainerPanel}.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public interface TransportOrderContainerListener {

  /**
   * Notifies the listener that the container has been initialized.
   *
   * @param orders The orders the container has been initialized with.
   */
  void containerInitialized(Collection<TransportOrder> orders);

  /**
   * Notifies the listener that a transport order has been added.
   *
   * @param order The transport order that has been added.
   */
  void transportOrderAdded(TransportOrder order);

  /**
   * Notifies the listener that a transport order has been updated.
   *
   * @param order The transport order that has been updated.
   */
  void transportOrderUpdated(TransportOrder order);

  /**
   * Notifies the listener that a transport order has been removed.
   *
   * @param order The transport order that has been removed.
   */
  void transportOrderRemoved(TransportOrder order);

}
