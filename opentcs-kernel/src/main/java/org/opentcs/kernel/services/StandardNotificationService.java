// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.services;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.List;
import java.util.function.Predicate;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.kernel.workingset.NotificationBuffer;

/**
 * This class is the standard implementation of the {@link NotificationService} interface.
 */
public class StandardNotificationService
    implements
      NotificationService {

  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The buffer for all messages published.
   */
  private final NotificationBuffer notificationBuffer;

  /**
   * Creates a new instance.
   *
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param notificationBuffer The notification buffer to be used.
   */
  @Inject
  public StandardNotificationService(
      @GlobalSyncObject
      Object globalSyncObject,
      NotificationBuffer notificationBuffer
  ) {
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.notificationBuffer = requireNonNull(notificationBuffer, "notificationBuffer");
  }

  @Override
  public List<UserNotification> fetchUserNotifications(Predicate<UserNotification> predicate) {
    synchronized (globalSyncObject) {
      return notificationBuffer.getNotifications(predicate);
    }
  }

  @Override
  public void publishUserNotification(UserNotification notification) {
    requireNonNull(notification, "notification");

    synchronized (globalSyncObject) {
      notificationBuffer.addNotification(notification);
    }
  }
}
