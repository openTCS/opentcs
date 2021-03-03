/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import java.io.IOException;
import java.util.Set;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.workingset.TransportOrderPool;

/**
 * Implementations of this interface provide ways to persist and archive
 * transport orders as well as loading persisted orders into the running system.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface OrderPersister {
  /**
   * Writes a transport order to the archive.
   * The transport order to be archived must be in state <code>FINISHED</code>.
   * <em>Note:</em> Consider using {@link #archiveTransportOrders(Set)
   * archiveTransportOrders()} instead of this method if more than one transport
   * order is to be archived, as it might perform a lot better, depending on the
   * actual implementation.
   *
   * @param order The transport order to be archived.
   * @throws IOException If a problem occurred while trying to write to the
   * archive.
   * @throws IllegalArgumentException If the given order is not in state
   * <code>FINISHED</code>.
   */
  void archiveTransportOrder(TransportOrder order)
  throws IOException, IllegalArgumentException;
  
  /**
   * Writes a set of transport orders to the archive.
   * All transport orders to be archived must be in state <code>FINISHED</code>.
   *
   * @param orders The set of transport orders to be archived.
   * @throws IOException If a problem occurred while trying to write to the
   * archive.
   * @throws IllegalArgumentException If any of the given orders are not in
   * state <code>FINISHED</code>. (None of the orders are written in that case.)
   */
  void archiveTransportOrders(Set<TransportOrder> orders)
  throws IOException, IllegalArgumentException;
  
  /**
   * Persists the transport orders in a given pool.
   * Persisted are all orders that are <em>not</em> in state
   * <code>FINISHED</code>.
   *
   * @param pool The pool containing the transport orders to be persisted.
   * @throws IOException If an exception occured while saving
   */
  void saveTransportOrders(TransportOrderPool pool)
  throws IOException;
  
  /**
   * Loads transport orders previously persisted into the running system.
   * 
   * @param pool The TransportOrderPool
   * @throws IOException If an exception occured while loading
   */
  void loadTransportOrders(TransportOrderPool pool)
  throws IOException;
}
