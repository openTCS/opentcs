/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import java.util.Set;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.order.TransportOrder;

/**
 * Declares the methods of transport order batch creators.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
interface OrderBatchCreator {

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
