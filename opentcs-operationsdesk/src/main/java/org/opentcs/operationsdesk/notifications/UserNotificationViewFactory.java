/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.notifications;

import org.opentcs.data.notification.UserNotification;

/**
 * Creates user notification related GUI components.
 */
public interface UserNotificationViewFactory {

  /**
   * Creates a new view for a user notification.
   *
   * @param notification The user notification to be shown.
   * @return A new view for a user notification.
   */
  UserNotificationView createUserNotificationView(UserNotification notification);

}
