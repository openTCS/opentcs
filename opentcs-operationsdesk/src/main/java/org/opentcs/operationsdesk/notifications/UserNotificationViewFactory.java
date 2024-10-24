// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
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
