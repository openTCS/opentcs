/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.transport.orders;

import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.operationsdesk.transport.sequences.OrderSequenceView;

/**
 * Creates transport order-related GUI components.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface TransportViewFactory {

  /**
   * Creates a new view for a transport order.
   *
   * @param order The transport order to be shown.
   * @return A new view for a transport order.
   */
  TransportOrderView createTransportOrderView(TransportOrder order);

  /**
   * Creates a new view for an order sequence.
   *
   * @param sequence The order sequence to be shown.
   * @return A new view for an order sequence.
   */
  OrderSequenceView createOrderSequenceView(OrderSequence sequence);
}
