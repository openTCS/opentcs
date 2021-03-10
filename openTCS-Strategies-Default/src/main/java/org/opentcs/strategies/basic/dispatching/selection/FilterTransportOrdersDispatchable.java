/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.selection;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;

/**
 * Filters transport orders that are dispatchable.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class FilterTransportOrdersDispatchable
    implements TransportOrderSelectionFilter {

  /**
   * The order service.
   */
  private final TCSObjectService objectService;
  /**
   * Stores reservations of orders for vehicles.
   */
  private final OrderReservationPool orderReservationPool;

  /**
   * Creates a new isntance.
   *
   * @param objectService The order service.
   * @param orderReservationPool Stores reservations of orders for vehicles.
   */
  @Inject
  public FilterTransportOrdersDispatchable(TCSObjectService objectService,
                                           OrderReservationPool orderReservationPool) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
  }

  @Override
  public boolean test(TransportOrder order) {
    return dispatchable(order);
  }

  private boolean dispatchable(TransportOrder order) {
    // We only want to check dispatchable transport orders.
    // Filter out transport orders that are intended for other vehicles.
    // Also filter out all transport orders with reservations. We assume that a check for reserved
    // orders has been performed already, and if any had been found, we wouldn't have been called.
    return order.hasState(TransportOrder.State.DISPATCHABLE)
        && !partOfOtherVehiclesSequence(order)
        && !orderReservationPool.isReserved(order.getReference());
  }

  private boolean partOfOtherVehiclesSequence(TransportOrder order) {
    if (order.getWrappingSequence() != null) {
      OrderSequence seq = objectService.fetchObject(OrderSequence.class,
                                                    order.getWrappingSequence());
      if (seq != null && seq.getProcessingVehicle() != null) {
        return true;
      }
    }
    return false;
  }
}
