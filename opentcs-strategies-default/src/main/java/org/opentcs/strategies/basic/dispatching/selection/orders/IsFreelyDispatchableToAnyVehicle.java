// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.selection.orders;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.function.Predicate;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;
import org.opentcs.strategies.basic.dispatching.selection.TransportOrderSelectionFilter;

/**
 * Filters transport orders that are dispatchable and available to <em>any</em> vehicle.
 *
 * <p>
 * Note: This filter is not a {@link TransportOrderSelectionFilter} by intention, since it is not
 * intended to be used in contexts where {@link ObjectHistory} entries are created.
 * </p>
 */
public class IsFreelyDispatchableToAnyVehicle
    implements
      Predicate<TransportOrder> {

  /**
   * The order service.
   */
  private final TCSObjectService objectService;
  /**
   * Stores reservations of orders for vehicles.
   */
  private final OrderReservationPool orderReservationPool;

  /**
   * Creates a new instance.
   *
   * @param objectService The order service.
   * @param orderReservationPool Stores reservations of orders for vehicles.
   */
  @Inject
  public IsFreelyDispatchableToAnyVehicle(
      TCSObjectService objectService,
      OrderReservationPool orderReservationPool
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
  }

  @Override
  public boolean test(TransportOrder order) {
    // We only want to check dispatchable transport orders.
    // Filter out transport orders that are intended for other vehicles.
    // Also filter out all transport orders with reservations. We assume that a check for reserved
    // orders has been performed already, and if any had been found, we wouldn't have been called.
    return order.hasState(TransportOrder.State.DISPATCHABLE)
        && !partOfAnyVehiclesSequence(order)
        && !orderReservationPool.isReserved(order.getReference());
  }

  private boolean partOfAnyVehiclesSequence(TransportOrder order) {
    if (order.getWrappingSequence() == null) {
      return false;
    }
    return objectService.fetch(OrderSequence.class, order.getWrappingSequence())
        .map(OrderSequence::getProcessingVehicle)
        .isPresent();
  }
}
