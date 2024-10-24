// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.transport.sequences;

import java.util.Collection;
import org.opentcs.data.order.OrderSequence;

/**
 *
 * Listener for changes in the {@link OrderSequencesContainerPanel}.
 */
public interface OrderSequenceContainerListener {

  /**
   * Notifies the listener that the container has been initialized.
   *
   * @param sequences The sequences the container has been initialized with.
   */
  void containerInitialized(Collection<OrderSequence> sequences);

  /**
   * Notifies the listener that an order sequence has been added.
   *
   * @param sequence The order sequence that has been added.
   */
  void orderSequenceAdded(OrderSequence sequence);

  /**
   * Notifies the listener that an order sequence has been updated.
   *
   * @param sequence The order sequence that has been updated.
   */
  void orderSequenceUpdated(OrderSequence sequence);

  /**
   * Notifies the listener that an order sequence has been removed.
   *
   * @param sequence The order sequence that has been removed.
   */
  void orderSequenceRemoved(OrderSequence sequence);
}
