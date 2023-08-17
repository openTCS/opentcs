/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.assignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import static org.opentcs.data.order.TransportOrderHistoryCodes.ORDER_ASSIGNED_TO_VEHICLE;
import static org.opentcs.data.order.TransportOrderHistoryCodes.ORDER_DISPATCHING_DEFERRED;
import static org.opentcs.data.order.TransportOrderHistoryCodes.ORDER_DISPATCHING_RESUMED;
import static org.opentcs.data.order.TransportOrderHistoryCodes.ORDER_RESERVED_FOR_VEHICLE;
import org.opentcs.strategies.basic.dispatching.phase.OrderFilterResult;

/**
 * Provides methods to check and update the dispatching status of transport orders.
 * <p>
 * Examples for a transport order's dispatching status:
 * </p>
 * <ul>
 * <li>Dispatching of a transport order has been deferred.</li>
 * <li>A transport order has been assigned to a vehicle.</li>
 * </ul>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DispatchingStatusMarker {

  private final TCSObjectService objectService;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service to use.
   */
  @Inject
  public DispatchingStatusMarker(TCSObjectService objectService) {
    this.objectService = requireNonNull(objectService, "objectService");
  }

  /**
   * Marks the {@link TransportOrder} referenced in the given {@link OrderFilterResult} as
   * deferred.
   *
   * @param filterResult The filter result.
   */
  public void markOrderAsDeferred(OrderFilterResult filterResult) {
    objectService.appendObjectHistoryEntry(
        filterResult.getOrder().getReference(),
        new ObjectHistory.Entry(
            ORDER_DISPATCHING_DEFERRED,
            Collections.unmodifiableList(new ArrayList<>(filterResult.getFilterReasons()))
        )
    );
  }

  /**
   * Marks the given {@link TransportOrder} as resumed.
   *
   * @param transportOrder The transport order.
   */
  public void markOrderAsResumed(TransportOrder transportOrder) {
    objectService.appendObjectHistoryEntry(
        transportOrder.getReference(),
        new ObjectHistory.Entry(
            ORDER_DISPATCHING_RESUMED,
            Collections.unmodifiableList(new ArrayList<>())
        )
    );
  }

  /**
   * Checks whether the given {@link TransportOrder} is marked as deferred regarding dispatching.
   *
   * @param transportOrder The transport order.
   * @return {@code true}, if the {@link ObjectHistory} of the given transport order indicates that
   * the transport order is currently being deferred regarding dispatching, otherwise {@code false}.
   */
  public boolean isOrderMarkedAsDeferred(TransportOrder transportOrder) {
    return lastRelevantDeferredHistoryEntry(transportOrder).isPresent();
  }

  /**
   * Marks the given {@link TransportOrder} as being assigned to the given {@link Vehicle}.
   *
   * @param transportOrder The transport order.
   * @param vehicle The vehicle.
   */
  public void markOrderAsAssigned(TransportOrder transportOrder, Vehicle vehicle) {
    objectService.appendObjectHistoryEntry(
        transportOrder.getReference(),
        new ObjectHistory.Entry(ORDER_ASSIGNED_TO_VEHICLE, vehicle.getName())
    );
  }

  /**
   * Marks the given {@link TransportOrder} as being reserved for the given {@link Vehicle}.
   *
   * @param transportOrder The transport order.
   * @param vehicle The vehicle.
   */
  public void markOrderAsReserved(TransportOrder transportOrder, Vehicle vehicle) {
    objectService.appendObjectHistoryEntry(
        transportOrder.getReference(),
        new ObjectHistory.Entry(ORDER_RESERVED_FOR_VEHICLE, vehicle.getName())
    );
  }

  /**
   * Checks whether the reasons for deferral of the {@link TransportOrder} referenced in the given
   * {@link OrderFilterResult} have changed in comparison to the (new)
   * {@link OrderFilterResult#getFilterReasons()}.
   *
   * @param filterResult The filter result.
   * @return {@code true}, if the reasons for deferral have changed, otherwise {@code false}.
   */
  @SuppressWarnings("unchecked")
  public boolean haveDeferralReasonsForOrderChanged(OrderFilterResult filterResult) {
    Collection<String> newReasons = filterResult.getFilterReasons();
    Collection<String> oldReasons = lastRelevantDeferredHistoryEntry(filterResult.getOrder())
        .map(entry -> (Collection<String>) entry.getSupplement())
        .orElse(new ArrayList<>());

    return newReasons.size() != oldReasons.size()
        || !newReasons.containsAll(oldReasons);
  }

  private Optional<ObjectHistory.Entry> lastRelevantDeferredHistoryEntry(
      TransportOrder transportOrder) {
    return transportOrder.getHistory().getEntries().stream()
        .filter(entry -> equalsAny(entry.getEventCode(),
                                   ORDER_DISPATCHING_DEFERRED,
                                   ORDER_DISPATCHING_RESUMED))
        .reduce((firstEntry, secondEntry) -> secondEntry)
        .filter(entry -> entry.getEventCode().equals(ORDER_DISPATCHING_DEFERRED));
  }

  private boolean equalsAny(String string, String... others) {
    return Arrays.asList(others).stream()
        .anyMatch(other -> string.equals(other));
  }
}
