// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.plugins.panels.loadgenerator.batchcreator;

import java.util.Set;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.order.TransportOrder;

/**
 * Declares the methods of transport order batch creators.
 */
public interface OrderBatchCreator {

  /**
   * Creates a new transport order batch.
   *
   * @return The created transport orders
   * @throws KernelRuntimeException In case the kernel threw an exception when
   * creating the transport orders.
   */
  Set<TransportOrder> createOrderBatch()
      throws KernelRuntimeException;
}
