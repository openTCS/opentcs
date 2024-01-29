/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.assignment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.phase.OrderFilterResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.opentcs.data.order.TransportOrderHistoryCodes.ORDER_DISPATCHING_DEFERRED;
import static org.opentcs.data.order.TransportOrderHistoryCodes.ORDER_DISPATCHING_RESUMED;

/**
 * Tests for {@link DispatchingStatusMarker}.
 */
class DispatchingStatusMarkerTest {

  private DispatchingStatusMarker dispatchingStatusMarker;

  @BeforeEach
  void setUp() {
    dispatchingStatusMarker = new DispatchingStatusMarker(mock(TCSObjectService.class));
  }

  @Test
  void evaluateOrderAsDeferred() {
    TransportOrder order = new TransportOrder("order", List.of())
        .withHistoryEntry(
            new ObjectHistory.Entry(ORDER_DISPATCHING_DEFERRED, List.of("some-reason"))
        );

    assertTrue(dispatchingStatusMarker.isOrderMarkedAsDeferred(order));
  }

  @ParameterizedTest
  @ValueSource(strings = {ORDER_DISPATCHING_RESUMED, "some-unrelated-history-event-code"})
  void evaluateOrderAsNotDeferred(String eventCode) {
    TransportOrder order = new TransportOrder("order", List.of())
        .withHistoryEntry(new ObjectHistory.Entry(eventCode, List.of("some-reason")));

    assertFalse(dispatchingStatusMarker.isOrderMarkedAsDeferred(order));
  }

  @Test
  void evaluateOrderDeferralReasonsAsChangedForNotDeferredOrder() {
    TransportOrder order = new TransportOrder("order", List.of());
    OrderFilterResult orderFilterResult = new OrderFilterResult(order, List.of("some-new-reason"));

    assertTrue(dispatchingStatusMarker.haveDeferralReasonsForOrderChanged(orderFilterResult));
  }

  @Test
  void evaluateOrderDeferralReasonsAsChangedForDeferredOrder() {
    TransportOrder order = new TransportOrder("order", List.of())
        .withHistoryEntry(
            new ObjectHistory.Entry(ORDER_DISPATCHING_DEFERRED, List.of("some-reason"))
        );
    OrderFilterResult orderFilterResult = new OrderFilterResult(order, List.of("some-new-reason"));

    assertTrue(dispatchingStatusMarker.haveDeferralReasonsForOrderChanged(orderFilterResult));
  }

  @Test
  void evaluateOrderDeferralReasonsAsChangedForResumedOrder() {
    TransportOrder order = new TransportOrder("order", List.of())
        .withHistoryEntry(
            new ObjectHistory.Entry(ORDER_DISPATCHING_DEFERRED, List.of("some-reason"))
        )
        .withHistoryEntry(new ObjectHistory.Entry(ORDER_DISPATCHING_RESUMED, List.of()));
    OrderFilterResult orderFilterResult = new OrderFilterResult(order, List.of("some-reason"));

    assertTrue(dispatchingStatusMarker.haveDeferralReasonsForOrderChanged(orderFilterResult));
  }

  @Test
  void evaluateOrderDeferralReasonsAsUnchangedForNotDeferredOrder() {
    TransportOrder order = new TransportOrder("order", List.of());
    OrderFilterResult orderFilterResult = new OrderFilterResult(order, List.of());

    assertFalse(dispatchingStatusMarker.haveDeferralReasonsForOrderChanged(orderFilterResult));
  }

  @Test
  void evaluateOrderDeferralReasonsAsUnchangedForDeferredOrder() {
    TransportOrder order = new TransportOrder("order", List.of())
        .withHistoryEntry(
            new ObjectHistory.Entry(ORDER_DISPATCHING_DEFERRED, List.of("some-reason"))
        );
    OrderFilterResult orderFilterResult = new OrderFilterResult(order, List.of("some-reason"));

    assertFalse(dispatchingStatusMarker.haveDeferralReasonsForOrderChanged(orderFilterResult));
  }

  @Test
  void evaluateOrderDeferralReasonsAsUnchangedForResumedOrder() {
    TransportOrder order = new TransportOrder("order", List.of())
        .withHistoryEntry(
            new ObjectHistory.Entry(ORDER_DISPATCHING_DEFERRED, List.of("some-reason"))
        )
        .withHistoryEntry(new ObjectHistory.Entry(ORDER_DISPATCHING_RESUMED, List.of()));
    OrderFilterResult orderFilterResult = new OrderFilterResult(order, List.of());

    assertFalse(dispatchingStatusMarker.haveDeferralReasonsForOrderChanged(orderFilterResult));
  }
}
