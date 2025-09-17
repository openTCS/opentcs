// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.watchdog;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.data.order.TransportOrder;

/**
 * Tests for {@link TransportOrderCheck}.
 */
public class TransportOrderCheckTest {
  private IdleAndExpiredTransportOrders idleAndExpiredTransportOrders;
  private TransportOrderCheck transportOrderCheck;
  private NotificationService notificationService;
  private WatchdogConfiguration watchdogConfiguration;
  private ScheduledExecutorService executorService;
  private TransportOrder order1;
  private IdleAndExpiredTransportOrders.TransportOrderSnapshot transportOrderSnapshot;

  @BeforeEach
  void setup() {
    notificationService = mock();
    watchdogConfiguration = mock();
    executorService = mock();
    idleAndExpiredTransportOrders = mock();


    order1 = new TransportOrder("T1", List.of()).withDeadline(Instant.now());
    transportOrderSnapshot = new IdleAndExpiredTransportOrders.TransportOrderSnapshot(order1);

    when(watchdogConfiguration.transportOrderCheckInterval()).thenReturn(1000);
    when(watchdogConfiguration.idleTransportOrderDurationThreshold()).thenReturn(300000);

    transportOrderCheck = new TransportOrderCheck(
        executorService,
        notificationService,
        watchdogConfiguration,
        idleAndExpiredTransportOrders
    );
  }

  @Test
  void idleTransportOrderShouldSendNotification() {
    when(idleAndExpiredTransportOrders.newlyIdleTransportOrders())
        .thenReturn(Set.of(transportOrderSnapshot));

    transportOrderCheck.run();

    verify(notificationService, times(1)).publishUserNotification(any());
  }

  @Test
  void expiredDeadlineTransportOrderShouldSendNotification() {
    when(idleAndExpiredTransportOrders.newlyExpiredTransportOrders())
        .thenReturn(Set.of(transportOrderSnapshot));

    transportOrderCheck.run();

    verify(notificationService, times(1)).publishUserNotification(any());
  }

  @Test
  void expiredDeadlineAndUnusedTransportOrderShouldSendNotification() {
    when(idleAndExpiredTransportOrders.newlyIdleTransportOrders())
        .thenReturn(Set.of(transportOrderSnapshot));
    when(idleAndExpiredTransportOrders.newlyExpiredTransportOrders())
        .thenReturn(Set.of(transportOrderSnapshot));

    transportOrderCheck.run();

    verify(notificationService, times(2)).publishUserNotification(any());
  }

  @Test
  void shouldNotSendNotificationOnNoIdleAndExpiredTransportOrders() {
    when(idleAndExpiredTransportOrders.newlyIdleTransportOrders())
        .thenReturn(Set.of());
    when(idleAndExpiredTransportOrders.newlyExpiredTransportOrders())
        .thenReturn(Set.of());

    transportOrderCheck.run();

    verify(notificationService, never()).publishUserNotification(any());
  }

  @Test
  void shouldSendNotificationOnFinalState() {
    when(idleAndExpiredTransportOrders.newTransportOrdersInFinalState())
        .thenReturn(Set.of(order1));

    transportOrderCheck.run();

    verify(notificationService, times(1)).publishUserNotification(any());
  }
}
