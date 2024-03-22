/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.watchdog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import static org.opentcs.data.model.Vehicle.IntegrationLevel.TO_BE_RESPECTED;
import static org.opentcs.data.model.Vehicle.IntegrationLevel.TO_BE_UTILIZED;
import org.opentcs.data.notification.UserNotification;

/**
 * Periodically checks the occupancy status of single-vehicle blocks.
 *
 * This check will publish a user notification if a single-vehicle block is occupied by more than
 * one vehicle.
 * A single notification will be published when the violation is first detected.
 * A second notification will be published when the violation changed or is resolved.
 *
 * The exact rules for when a violation notification should be sent are:
 *
 * <ul>
 * <li> A violation should trigger a notification if a block is occupied by more than one vehicle
 * and the set of vehicles occupying the block is different to the one in the previous iteration.
 * The order of the occupants does not matter.
 * (V1, V2) is the same as (V2, V1).</li>
 * <li> If a block was previously occupied by more than one vehicle and is now occupied by one or no
 * vehicle, a notification about the resultion of the situation is sent.</li>
 * </ul>
 *
 * Examples:
 *
 * <ul>
 * <li> A block previously occupied by (V1, V2) that is now occupied by (V1, V2, V3) should
 * trigger a new violation notification.</li>
 * <li> A block previously occupied by (V1, V2) that is now occupied by (V1) should trigger a
 * resolution notification.</li>
 * <li> A block previously occupied by (V1, V2) that is now still occupied by (V1, V2) or (V2, V1)
 * should not trigger a new notification.</li>
 * </ul>
 */
public class BlockConsistencyCheck
    implements Runnable,
               Lifecycle {

  /**
   * Notification source.
   */
  private static final String NOTIFICATION_SOURCE = "Watchdog - Block consistency check";
  /**
   * Object service to access the model.
   */
  private final TCSObjectService objectService;
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
   * Whether this check is initialized.
   */
  private boolean initialized;
  /**
   * The Future created for the block check task.
   */
  private ScheduledFuture<?> scheduledFuture;
  /**
   * Holds currently known block occupations.
   * Maps a block reference to a set of vehicles contained in that block.
   */
  private Map<TCSResourceReference<Block>, Set<TCSObjectReference<Vehicle>>> occupations
      = new HashMap<>();

  /**
   * Creates a new instance.
   *
   * @param kernelExecutor The kernel executor.
   * @param objectService The object service.
   * @param notificationService The notification service.
   * @param configuration The watchdog configuration.
   */
  @Inject
  public BlockConsistencyCheck(@KernelExecutor ScheduledExecutorService kernelExecutor,
                    TCSObjectService objectService,
                    NotificationService notificationService,
                    WatchdogConfiguration configuration) {
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.objectService = requireNonNull(objectService, "objectService");
    this.notificationService = requireNonNull(notificationService, "notificationService");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    scheduledFuture = kernelExecutor.scheduleAtFixedRate(
        this,
        configuration.blockConsistencyCheckInterval(),
        configuration.blockConsistencyCheckInterval(),
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

    initialized = false;
  }

  @Override
  public void run() {
    Map<TCSResourceReference<Block>, Set<TCSObjectReference<Vehicle>>> currentOccupations
        = findCurrentOccupations();

    // Find new violations.
    currentOccupations.entrySet().stream()
        .filter(entry -> entry.getValue().size() > 1)
        .filter(entry -> {
          return !occupations.containsKey(entry.getKey())
              || !occupations.get(entry.getKey()).equals(entry.getValue());
        })
        .forEach(entry -> {
          notificationService.publishUserNotification(new UserNotification(
              NOTIFICATION_SOURCE,
              String.format(
                  "Block %s is overfull. Occupied by vehicles: %s",
                  entry.getKey().getName(),
                  entry.getValue().stream()
                      .map(vehicle -> vehicle.getName())
                      .collect(Collectors.joining(", "))
              ),
              UserNotification.Level.IMPORTANT
          ));
        });

    // Find resolved violations
    occupations.entrySet().stream()
        .filter(entry -> entry.getValue().size() > 1)
        .filter(entry -> {
          return !currentOccupations.containsKey(entry.getKey())
              || currentOccupations.get(entry.getKey()).size() <= 1;
        })
        .forEach(entry -> {
          notificationService.publishUserNotification(new UserNotification(
              NOTIFICATION_SOURCE,
              String.format("Block %s is not overfull any more.", entry.getKey().getName()),
              UserNotification.Level.IMPORTANT
          ));
        });

    occupations = currentOccupations;
  }

  private Map<TCSResourceReference<Block>, Set<TCSObjectReference<Vehicle>>>
      findCurrentOccupations() {
    Map<TCSResourceReference<Block>, Set<TCSObjectReference<Vehicle>>> currentOccupations
        = new HashMap<>();

    Set<Block> blocks = objectService.fetchObjects(Block.class);

    objectService.fetchObjects(Vehicle.class)
        .stream()
        .filter(vehicle -> {
          return vehicle.getIntegrationLevel() == TO_BE_RESPECTED
              || vehicle.getIntegrationLevel() == TO_BE_UTILIZED;
        })
        .filter(vehicle -> vehicle.getCurrentPosition() != null)
        .forEach(vehicle -> {
          Point currentPoint = objectService.fetchObject(Point.class, vehicle.getCurrentPosition());

          blocks.stream()
              .filter(block -> block.getType() == Block.Type.SINGLE_VEHICLE_ONLY)
              .filter(block -> block.getMembers().contains(currentPoint.getReference()))
              .forEach(block -> {
                currentOccupations.putIfAbsent(block.getReference(), new HashSet<>());
                currentOccupations.get(block.getReference()).add(vehicle.getReference());
              });
        });

    return currentOccupations;
  }
}
