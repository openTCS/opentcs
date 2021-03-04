/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Instances of this class represent events emitted by/for notifications being published.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Use {@link NotificationPublicationEvent} instead, which does not extend TCSEvent.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
public class TCSNotificationEvent
    extends org.opentcs.util.eventsystem.TCSEvent
    implements Serializable {

  /**
   * The published message.
   */
  private final UserNotification notification;

  /**
   * Creates a new instance.
   *
   * @param message The message being published.
   */
  public TCSNotificationEvent(UserNotification message) {
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
    return "TCSNotificationEvent{" + "notification=" + notification + '}';
  }
}
