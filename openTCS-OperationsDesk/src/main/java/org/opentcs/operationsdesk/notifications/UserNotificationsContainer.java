/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.notifications;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.NotificationPublicationEvent;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.guing.common.event.OperationModeChangeEvent;
import org.opentcs.guing.common.event.SystemModelTransitionEvent;
import org.opentcs.operationsdesk.event.KernelStateChangeEvent;
import org.opentcs.operationsdesk.util.PlantOverviewOperatingApplicationConfiguration;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains a list of the most recent user notifications.
 */
public class UserNotificationsContainer
    implements EventHandler,
               Lifecycle {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(UserNotificationsContainer.class);
  /**
   * Where we get events from.
   */
  private final EventBus eventBus;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * The user notifications.
   */
  private final List<UserNotification> userNotifications = new LinkedList<>();
  /**
   * This container's listeners.
   */
  private final Set<UserNotificationContainerListener> listeners = new HashSet<>();
  /**
   * The amount of user notifications to be kept in the container.
   * Configurable through the operation desk's configuration.
   */
  private final int capacity;
  /**
   * Whether this component is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param eventBus Where this instance subscribes for events.
   * @param portalProvider Provides access to a portal.
   * @param configuration The operations desk application's configuration.
   */
  @Inject
  public UserNotificationsContainer(@ApplicationEventBus EventBus eventBus,
                                    SharedKernelServicePortalProvider portalProvider,
                                    PlantOverviewOperatingApplicationConfiguration configuration) {
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.capacity = requireNonNull(configuration, "configuration").userNotificationDisplayCount();
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    eventBus.subscribe(this);

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    eventBus.unsubscribe(this);

    initialized = false;
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof NotificationPublicationEvent) {
      handleNotificationEvent((NotificationPublicationEvent) event);
    }
    else if (event instanceof OperationModeChangeEvent) {
      initNotifications();
    }
    else if (event instanceof SystemModelTransitionEvent) {
      initNotifications();
    }
    else if (event instanceof KernelStateChangeEvent) {
      initNotifications();
    }
  }

  public void addListener(UserNotificationContainerListener listener) {
    listeners.add(listener);
  }

  public void removeListener(UserNotificationContainerListener listener) {
    listeners.remove(listener);
  }

  /**
   * Returns the user notification with the given index, if it exists.
   *
   * @param index The index of the user notification.
   * @return The user notification with the given index, if it exists.
   */
  public Optional<UserNotification> getUserNotification(int index) {
    return Optional.ofNullable(userNotifications.get(index));
  }

  /**
   * Returns all currently stored user notifications.
   *
   * @return The collection of user notifications.
   */
  public List<UserNotification> getUserNotifications() {
    return userNotifications;
  }

  private void initNotifications() {
    setUserNotifications(fetchNotificationsIfOnLine());
    while (userNotifications.size() > capacity) {
      userNotifications.remove(0);
    }
    listeners.forEach(listener -> listener.containerInitialized(userNotifications));
  }

  private List<UserNotification> fetchNotificationsIfOnLine() {
    if (portalProvider.portalShared()) {
      try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
        return sharedPortal.getPortal().getNotificationService()
            .fetchUserNotifications(null);
      }
      catch (KernelRuntimeException exc) {
        LOG.warn("Exception fetching user notifications", exc);
      }
    }

    return List.of();
  }

  private void handleNotificationEvent(NotificationPublicationEvent evt) {
    userNotifications.add(evt.getNotification());
    listeners.forEach(listener -> listener.userNotificationAdded(evt.getNotification()));

    while (userNotifications.size() > capacity) {
      UserNotification removedNotification = userNotifications.remove(0);
      listeners.forEach(listener -> listener.userNotificationRemoved(removedNotification));
    }
  }

  private void setUserNotifications(List<UserNotification> newNotifications) {
    userNotifications.clear();
    userNotifications.addAll(newNotifications);
  }
}
