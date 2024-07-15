/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.notifications;

import java.util.List;
import org.opentcs.data.notification.UserNotification;

/**
 * Listener for changes in the {@link UserNotificationsContainerPanel}.
 */
public interface UserNotificationContainerListener {

  /**
   * Notifies the listener that the container has been initialized.
   *
   * @param notifications The notifications the container has been initialized with.
   */
  void containerInitialized(List<UserNotification> notifications);

  /**
   * Notifies the listener that a user notification has been added.
   *
   * @param notification The user notification that has been added.
   */
  void userNotificationAdded(UserNotification notification);

  /**
   * Notifies the listener that a user notification has been removed.
   *
   * @param notification The removed notification.
   */
  void userNotificationRemoved(UserNotification notification);

}
