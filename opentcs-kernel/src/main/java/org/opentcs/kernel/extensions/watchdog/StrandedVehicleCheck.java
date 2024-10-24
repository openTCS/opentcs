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
import org.opentcs.kernel.extensions.watchdog.StrandedVehicles.VehicleSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks for vehicles that are stranded for too long.
 * <p>
 * A vehicle is considered "stranded" if one of the following conditions applies and lasts longer
 * than a configurable time period (see
 * {@link WatchdogConfiguration#strandedVehicleDurationThreshold()}):
 * </p>
 * <ul>
 * <li>The vehicle is idle and at a position that is <em>not</em> a parking position.</li>
 * <li>The vehicle is idle and has a transport order assigned to it.</li>
 * </ul>
 * This check publishes a user notification when a vehicle is considered stranded and another user
 * notification once a vehicle is no longer considered stranded.
 */
public class StrandedVehicleCheck
    implements
      Runnable,
      Lifecycle {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StrandedVehicleCheck.class);
  /**
   * Source for notifications.
   */
  private static final String NOTIFICATION_SOURCE = "Watchdog - Stranded vehicle check";
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
   * Keeps track of stranded vehicles.
   */
  private final StrandedVehicles stranded;
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
   * @param stranded Keeps track of stranded vehicles.
   */
  @Inject
  public StrandedVehicleCheck(
      @KernelExecutor
      ScheduledExecutorService kernelExecutor,
      NotificationService notificationService,
      WatchdogConfiguration configuration,
      StrandedVehicles stranded
  ) {
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.notificationService = requireNonNull(notificationService, "notificationService");
    this.configuration = requireNonNull(configuration, "configuration");
    this.stranded = requireNonNull(stranded, "stranded");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    scheduledFuture = kernelExecutor.scheduleAtFixedRate(
        this,
        configuration.strandedVehicleCheckInterval(),
        configuration.strandedVehicleCheckInterval(),
        TimeUnit.MILLISECONDS
    );

    stranded.initialize();
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

    stranded.terminate();
    initialized = false;
  }

  @Override
  public void run() {
    long currentTime = System.currentTimeMillis();
    long strandedTimeThreshold = configuration.strandedVehicleDurationThreshold();

    stranded.identifyStrandedVehicles(currentTime, strandedTimeThreshold);
    Set<VehicleSnapshot> newlyStrandedVehicles = stranded.newlyStrandedVehicles();
    Set<VehicleSnapshot> noLongerStrandedVehicles = stranded.noLongerStrandedVehicles();

    newlyStrandedVehicles
        .forEach(vehicleSnapshot -> {
          notificationService.publishUserNotification(
              new UserNotification(
                  NOTIFICATION_SOURCE,
                  String.format(
                      "Vehicle '%s' is stranded since: %s",
                      vehicleSnapshot.getVehicle().getName(),
                      TIMESTAMP_FORMATTER.format(
                          Instant.ofEpochMilli(vehicleSnapshot.getLastRelevantStateChange())
                      )
                  ),
                  UserNotification.Level.INFORMATIONAL
              )
          );
        });

    noLongerStrandedVehicles
        .forEach(vehicleSnapshot -> {
          notificationService.publishUserNotification(
              new UserNotification(
                  NOTIFICATION_SOURCE,
                  String.format(
                      "Vehicle '%s' is no longer stranded.",
                      vehicleSnapshot.getVehicle().getName()
                  ),
                  UserNotification.Level.INFORMATIONAL
              )
          );
        });
  }
}
