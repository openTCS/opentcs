// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.transport.orders;

import java.util.Collection;
import org.opentcs.data.order.TransportOrder;

/**
 * Listener for changes in the {@link TransportOrdersContainerPanel}.
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
