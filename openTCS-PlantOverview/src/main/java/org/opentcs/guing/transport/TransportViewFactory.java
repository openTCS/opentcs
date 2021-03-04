/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.transport;

import org.opentcs.data.order.OrderSequence;

/**
 * Creates transport order-related GUI components.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface TransportViewFactory {

  /**
   * Creates a new view for an order sequence.
   *
   * @param sequence The order sequence to be shown.
   * @return A new view for an order sequence.
   */
  OrderSequenceView createOrderSequenceView(OrderSequence sequence);
}
