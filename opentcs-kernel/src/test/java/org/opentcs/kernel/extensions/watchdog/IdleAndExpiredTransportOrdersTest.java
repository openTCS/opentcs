// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.watchdog;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.TimeProvider;

/**
 * Test for {@link IdleAndExpiredTransportOrders}.
 */
public class IdleAndExpiredTransportOrdersTest {
  private TCSObjectService objectService;
  private TimeProvider timeProvider;
  private IdleAndExpiredTransportOrders idleAndExpiredTransportOrders;

  @BeforeEach
  void setUp() {
    objectService = mock(TCSObjectService.class);
    timeProvider = mock(TimeProvider.class);
    when(timeProvider.getCurrentTimeEpochMillis()).thenReturn(0L);
    idleAndExpiredTransportOrders = new IdleAndExpiredTransportOrders(objectService, timeProvider);
  }

  @Test
  void testIdentifyIdleAndExpiredTransportOrders() {
    TransportOrder orderInFinalState = new TransportOrder("T1", List.of())
        .withState(TransportOrder.State.FINISHED);
    TransportOrder orderCurrentlyProcessed = new TransportOrder("T2", List.of())
        .withState(TransportOrder.State.BEING_PROCESSED);
    TransportOrder orderIdle = new TransportOrder("T3", List.of());
    TransportOrder orderWithExpiredDeadline = new TransportOrder("T4", List.of())
        .withState(TransportOrder.State.BEING_PROCESSED)
        .withDeadline(Instant.ofEpochMilli(5000));
    TransportOrder orderIdleAndWithExpiredDeadline = new TransportOrder("T5", List.of())
        .withDeadline(Instant.ofEpochMilli(12000));

    when(objectService.fetch(TransportOrder.class))
        .thenReturn(
            Set.of(
                orderInFinalState,
                orderCurrentlyProcessed,
                orderIdle,
                orderWithExpiredDeadline,
                orderIdleAndWithExpiredDeadline
            )
        );
    idleAndExpiredTransportOrders.initialize();

    long idleDurationThreshold = 300000;
    long firstInvocationTime = 10000;
    long secondInvocationTime = firstInvocationTime + idleDurationThreshold + 1000;


    idleAndExpiredTransportOrders.identifyIdleOrExpiredTransportOrders(
        firstInvocationTime, idleDurationThreshold
    );
    // After the first invocation (when not exceeding the idle duration threshold), the transport
    // orders should not be considered idle.
    Set<IdleAndExpiredTransportOrders.TransportOrderSnapshot> resultIdle
        = idleAndExpiredTransportOrders.newlyIdleTransportOrders();
    assertTrue(resultIdle.isEmpty());
    // After the first invocation only transport order T4 should be considered expired.
    Set<IdleAndExpiredTransportOrders.TransportOrderSnapshot> resultExpiredDeadline
        = idleAndExpiredTransportOrders.newlyExpiredTransportOrders();
    assertThat(resultExpiredDeadline, hasSize(1));
    assertThat(
        resultExpiredDeadline
            .stream()
            .map(IdleAndExpiredTransportOrders.TransportOrderSnapshot::getTransportOrder).toList(),
        contains(orderWithExpiredDeadline)
    );

    idleAndExpiredTransportOrders.identifyIdleOrExpiredTransportOrders(
        secondInvocationTime, idleDurationThreshold
    );
    resultIdle = idleAndExpiredTransportOrders.newlyIdleTransportOrders();
    assertThat(resultIdle, hasSize(2));
    assertThat(
        resultIdle
            .stream()
            .map(IdleAndExpiredTransportOrders.TransportOrderSnapshot::getTransportOrder).toList(),
        containsInAnyOrder(orderIdle, orderIdleAndWithExpiredDeadline)
    );
    // After the second invocation only transport order T5 should be considered expired,
    // because T4 and T6 were previously already considered expired.
    resultExpiredDeadline = idleAndExpiredTransportOrders.newlyExpiredTransportOrders();
    assertThat(resultExpiredDeadline, hasSize(1));
    assertThat(
        resultExpiredDeadline
            .stream()
            .map(IdleAndExpiredTransportOrders.TransportOrderSnapshot::getTransportOrder).toList(),
        contains(orderIdleAndWithExpiredDeadline)
    );
  }

  @Test
  void doNotConsiderTransportOrdersWithARelevantStateChangeAsIdle() {
    TransportOrder order1 = new TransportOrder("T1", List.of());
    TransportOrder order2 = new TransportOrder("T2", List.of());
    when(objectService.fetch(TransportOrder.class))
        .thenReturn(Set.of(order1, order2));
    idleAndExpiredTransportOrders.initialize();

    long idleDurationThreshold = 300000;
    long firstInvocationTime = 10000;
    long secondInvocationTime = firstInvocationTime + idleDurationThreshold + 1000;

    idleAndExpiredTransportOrders.identifyIdleOrExpiredTransportOrders(
        firstInvocationTime,
        idleDurationThreshold
    );
    Set<IdleAndExpiredTransportOrders.TransportOrderSnapshot> resultIdle
        = idleAndExpiredTransportOrders.newlyIdleTransportOrders();
    assertTrue(resultIdle.isEmpty());

    order1 = order1.withState(TransportOrder.State.ACTIVE);
    order2 = order2.withProcessingVehicle(new Vehicle("V1").getReference());
    when(objectService.fetch(TransportOrder.class))
        .thenReturn(Set.of(order1, order2));
    idleAndExpiredTransportOrders.identifyIdleOrExpiredTransportOrders(
        secondInvocationTime,
        idleDurationThreshold
    );
    resultIdle = idleAndExpiredTransportOrders.newlyIdleTransportOrders();
    assertTrue(resultIdle.isEmpty());
  }

  @Test
  void testTransportOrdersInFinalStateThatWerePreviouslyIdleOrExpired() {
    TransportOrder orderIdle = new TransportOrder("T1", List.of());
    TransportOrder orderIdle2 = new TransportOrder("T2", List.of());
    TransportOrder orderWithExpiredDeadline = new TransportOrder("T3", List.of())
        .withState(TransportOrder.State.BEING_PROCESSED)
        .withDeadline(Instant.ofEpochMilli(5000));

    when(objectService.fetch(TransportOrder.class))
        .thenReturn(
            Set.of(
                orderIdle,
                orderIdle2,
                orderWithExpiredDeadline
            )
        );
    idleAndExpiredTransportOrders.initialize();

    long idleDurationThreshold = 1000;
    long firstInvocationTime = 15000;
    long secondInvocationTime = firstInvocationTime + idleDurationThreshold + 1000;
    long thirdInvocationTime = secondInvocationTime + 1000;

    idleAndExpiredTransportOrders.identifyIdleOrExpiredTransportOrders(
        firstInvocationTime, idleDurationThreshold
    );
    idleAndExpiredTransportOrders.newTransportOrdersInFinalState();
    Set<IdleAndExpiredTransportOrders.TransportOrderSnapshot> resultIdle
        = idleAndExpiredTransportOrders.newlyIdleTransportOrders();
    assertThat(resultIdle, hasSize(2));
    assertThat(
        resultIdle
            .stream()
            .map(IdleAndExpiredTransportOrders.TransportOrderSnapshot::getTransportOrder).toList(),
        containsInAnyOrder(orderIdle, orderIdle2)
    );
    Set<IdleAndExpiredTransportOrders.TransportOrderSnapshot> resultExpiredDeadline
        = idleAndExpiredTransportOrders.newlyExpiredTransportOrders();
    assertThat(resultExpiredDeadline, hasSize(1));
    assertThat(
        resultExpiredDeadline
            .stream()
            .map(IdleAndExpiredTransportOrders.TransportOrderSnapshot::getTransportOrder).toList(),
        contains(orderWithExpiredDeadline)
    );

    orderIdle = orderIdle.withState(TransportOrder.State.FAILED);
    orderIdle2 = orderIdle2.withState(TransportOrder.State.BEING_PROCESSED);
    orderWithExpiredDeadline = orderWithExpiredDeadline.withState(TransportOrder.State.FINISHED);

    when(objectService.fetch(TransportOrder.class))
        .thenReturn(
            Set.of(
                orderIdle,
                orderIdle2,
                orderWithExpiredDeadline
            )
        );

    idleAndExpiredTransportOrders.identifyIdleOrExpiredTransportOrders(
        secondInvocationTime, idleDurationThreshold
    );
    Set<TransportOrder> transportOrdersInFinalState
        = idleAndExpiredTransportOrders.newTransportOrdersInFinalState();
    assertThat(transportOrdersInFinalState, hasSize(2));
    assertThat(
        transportOrdersInFinalState, containsInAnyOrder(orderIdle, orderWithExpiredDeadline)
    );

    // After the third invocation there should be only one transport order that changed to a
    // final state since the last invocation.
    orderIdle2 = orderIdle2.withState(TransportOrder.State.FINISHED);
    when(objectService.fetch(TransportOrder.class))
        .thenReturn(
            Set.of(
                orderIdle,
                orderIdle2,
                orderWithExpiredDeadline
            )
        );

    idleAndExpiredTransportOrders.identifyIdleOrExpiredTransportOrders(
        thirdInvocationTime, idleDurationThreshold
    );
    transportOrdersInFinalState
        = idleAndExpiredTransportOrders.newTransportOrdersInFinalState();
    assertThat(transportOrdersInFinalState, hasSize(1));
    assertThat(transportOrdersInFinalState, contains(orderIdle2));
  }
}
