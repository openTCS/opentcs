// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import org.opentcs.data.notification.UserNotification;

/**
 * Instances of this class represent events emitted by/for notifications being published.
 */
public class NotificationPublicationEvent
    implements
      Serializable {

  /**
   * The published message.
   */
  private final UserNotification notification;

  /**
   * Creates a new instance.
   *
   * @param message The message being published.
   */
  public NotificationPublicationEvent(UserNotification message) {
    this.notification = requireNonNull(message, "notification");
  }

  /**
   * Returns the message being published.
   *
   * @return The message being published.
   */
  public UserNotification getNotification() {
    return notification;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + '{'
        + "notification=" + notification
        + '}';
  }
}
