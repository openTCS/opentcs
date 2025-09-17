// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.watchdog;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.data.order.TransportOrder;

/**
 * Periodically checks the state of transport orders.
 * <p>
 * This check publishes a user notification for transport orders in the following situations:
 * </p>
 * <ul>
 * <li>The transport order is considered "idle". A transport order is considered "idle" if it
 * is not in a final state, is not currently being processed by a vehicle and these conditions
 * last longer than a configurable time period (see
 * {@link WatchdogConfiguration#idleTransportOrderDurationThreshold()}).</li>
 * <li>The transport order's deadline has expired.</li>
 * </ul>
 * If a user notification for a transport order has been published for at least one of these
 * situations, another user notification is also published once that transport order has
 * reached a final state.
 */
public class TransportOrderCheck
    implements
      Runnable,
      Lifecycle {
  /**
   * Source for notifications.
   */
  private static final String NOTIFICATION_SOURCE = "Watchdog - Transport order check";
  /**
   * A formatter for timestamps.
   */
  private static final DateTimeFormatter TIMESTAMP_FORMATTER
      = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
  /**
   * The service to send out user notifications.
   */
  private final NotificationService notificationService;
  /**
   * The kernel executor.
   */
  private final ScheduledExecutorService kernelExecutor;
  /**
   * The configuration.
   */
  private final WatchdogConfiguration configuration;
  /**
   * Keeps track of idle/unused transport orders.
   */
  private final IdleAndExpiredTransportOrders idleAndExpiredTransportOrders;
  /**
   * Whether this check is initialized.
   */
  private boolean initialized;
  /**
   * The Future created for the block check task.
   */
  private ScheduledFuture<?> scheduledFuture;

  /**
   * Creates a new instance.
   *
   * @param kernelExecutor The kernel executor.
   * @param notificationService The notification service.
   * @param configuration The watchdog configuration.
   * @param idleAndExpiredTransportOrders Keeps track of idle/unused transport orders.
   */
  @Inject
  public TransportOrderCheck(
      @KernelExecutor
      ScheduledExecutorService kernelExecutor,
      NotificationService notificationService,
      WatchdogConfiguration configuration,
      IdleAndExpiredTransportOrders idleAndExpiredTransportOrders
  ) {
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.notificationService = requireNonNull(notificationService, "notificationService");
    this.configuration = requireNonNull(configuration, "configuration");
    this.idleAndExpiredTransportOrders = requireNonNull(
        idleAndExpiredTransportOrders, "idleAndExpiredTransportOrders"
    );
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    idleAndExpiredTransportOrders.initialize();

    scheduledFuture = kernelExecutor.scheduleAtFixedRate(
        this,
        configuration.transportOrderCheckInterval(),
        configuration.transportOrderCheckInterval(),
        TimeUnit.MILLISECONDS
    );
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    if (scheduledFuture != null) {
      scheduledFuture.cancel(true);
      scheduledFuture = null;
    }

    idleAndExpiredTransportOrders.terminate();
    initialized = false;
  }

  @Override
  public void run() {
    long currentTime = System.currentTimeMillis();
    long idleTimeThreshold = configuration.idleTransportOrderDurationThreshold();

    idleAndExpiredTransportOrders.identifyIdleOrExpiredTransportOrders(
        currentTime, idleTimeThreshold
    );
    Set<IdleAndExpiredTransportOrders.TransportOrderSnapshot> newlyIdleTransportOrders
        = idleAndExpiredTransportOrders.newlyIdleTransportOrders();
    Set<IdleAndExpiredTransportOrders.TransportOrderSnapshot> newlyExpiredTransportOrders
        = idleAndExpiredTransportOrders.newlyExpiredTransportOrders();
    Set<TransportOrder> newTransportOrdersInFinalState
        = idleAndExpiredTransportOrders.newTransportOrdersInFinalState();

    newlyIdleTransportOrders
        .forEach(transportOrderSnapshot -> {
          notificationService.publishUserNotification(
              new UserNotification(
                  NOTIFICATION_SOURCE,
                  String.format(
                      "Transport order '%s' is waiting to be processed and idle since: %s",
                      transportOrderSnapshot.getTransportOrder().getName(),
                      TIMESTAMP_FORMATTER.format(
                          Instant.ofEpochMilli(
                              transportOrderSnapshot.getLastRelevantStateChange()
                          )
                      )
                  ),
                  UserNotification.Level.INFORMATIONAL
              )
          );
        });

    newlyExpiredTransportOrders.forEach(transportOrderSnapshot -> {
      notificationService.publishUserNotification(
          new UserNotification(
              NOTIFICATION_SOURCE,
              String.format(
                  "The deadline ('%s') for transport order '%s' has expired.",
                  TIMESTAMP_FORMATTER.format(
                      transportOrderSnapshot.getTransportOrder().getDeadline()
                  ),
                  transportOrderSnapshot.getTransportOrder().getName()
              ),
              UserNotification.Level.INFORMATIONAL
          )
      );
    });

    newTransportOrdersInFinalState
        .forEach(
            order -> notificationService.publishUserNotification(
                new UserNotification(
                    NOTIFICATION_SOURCE,
                    String.format(
                        "Transport order '%s' has reached a final state.",
                        order.getName()
                    ),
                    UserNotification.Level.INFORMATIONAL
                )
            )
        );
  }
}
