/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.util.event.SimpleEventBus;

/**
 * Tests for {@link NotificationBuffer}.
 */
class NotificationBufferTest {

  private NotificationBuffer notificationBuffer;

  private UserNotification notification1;
  private UserNotification notification2;
  private UserNotification notification3;
  private UserNotification notification4;

  @BeforeEach
  void setUp() {
    notificationBuffer = new NotificationBuffer(new SimpleEventBus());

    notification1 = new UserNotification("notification-1", UserNotification.Level.NOTEWORTHY);
    notification2 = new UserNotification("notification-2", UserNotification.Level.NOTEWORTHY);
    notification3 = new UserNotification("notification-3", UserNotification.Level.NOTEWORTHY);
    notification4 = new UserNotification("notification-4", UserNotification.Level.NOTEWORTHY);
  }

  @Test
  void keepAllNotificationsBeforeOverflow() {
    notificationBuffer.setCapacity(3);

    notificationBuffer.addNotification(notification1);
    notificationBuffer.addNotification(notification2);
    notificationBuffer.addNotification(notification3);

    assertThat(notificationBuffer.getNotifications(null),
               contains(notification1, notification2, notification3));
  }

  @Test
  void keepYoungestNotificationsAfterOverflow() {
    notificationBuffer.setCapacity(3);

    notificationBuffer.addNotification(notification1);
    notificationBuffer.addNotification(notification2);
    notificationBuffer.addNotification(notification3);
    notificationBuffer.addNotification(notification4);

    assertThat(notificationBuffer.getNotifications(null),
               contains(notification2, notification3, notification4));
  }

  @Test
  void removeExtraNotificationsOnCapacityReduction() {
    notificationBuffer.setCapacity(4);

    notificationBuffer.addNotification(notification1);
    notificationBuffer.addNotification(notification2);
    notificationBuffer.addNotification(notification3);
    notificationBuffer.addNotification(notification4);

    assertThat(notificationBuffer.getNotifications(null), hasSize(4));
    assertThat(notificationBuffer.getNotifications(null),
               contains(notification1, notification2, notification3, notification4));

    notificationBuffer.setCapacity(2);

    assertThat(notificationBuffer.getNotifications(null), hasSize(2));
    assertThat(notificationBuffer.getNotifications(null), contains(notification3, notification4));
  }

  @Test
  void removeAllNotificationsOnClear() {
    notificationBuffer.setCapacity(3);
    notificationBuffer.addNotification(notification1);
    notificationBuffer.addNotification(notification2);

    notificationBuffer.clear();

    assertThat(notificationBuffer.getNotifications(null), is(empty()));
  }

  @Test
  void returnNotificationsMatchingFilter() {
    notificationBuffer.setCapacity(3);

    notificationBuffer.addNotification(notification1);
    notificationBuffer.addNotification(notification2);
    notificationBuffer.addNotification(notification3);
    notificationBuffer.addNotification(notification4);

    assertThat(
        notificationBuffer.getNotifications(
            notification -> notification.getText().equals("notification-2")
        ),
        contains(notification2)
    );
  }

}
