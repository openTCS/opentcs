/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.function.Predicate;
import javax.inject.Inject;
import org.opentcs.access.NotificationPublicationEvent;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A buffer which can store a (configurable) limited number of
 * {@link UserNotification Message} objects.
 * <p>
 * The actual size of a buffer can be influenced by two parameters, its
 * <code>capacity</code> and <code>cut back count</code>. When a new message is
 * added to the buffer and the number of messages in the buffer exceeds its
 * <code>capacity</code>, messages are removed from from the buffer until it
 * contains not more than <code>cut back count</code>.
 * </p>
 * <p>
 * Which messages are removed is decided by sorting all messages with the
 * <code>Comparator</code> given to the constructor. The last/highest elements
 * according to the sorting order of that <code>Comparator</code> are removed.
 * </p>
 * <p>
 * Note that no synchronization is done inside this class. Concurrent access of
 * instances of this class must be synchronized externally.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class NotificationBuffer {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(NotificationBuffer.class);
  /**
   * The actual messages.
   */
  private final List<UserNotification> notifications = new LinkedList<>();
  /**
   * The maximum number of messages that should be kept in this buffer.
   */
  private int capacity = 500;
  /**
   * The number of messages that will be kept when this buffer's capacity is
   * exceeded and messages are removed.
   */
  private int cutBackCount;
  /**
   * A listener for events concerning the stored messages.
   */
  private final EventHandler messageEventListener;

  /**
   * Creates a new instance that uses the given event listener.
   *
   * @param eventListener The event listener to be used.
   */
  @Inject
  public NotificationBuffer(@ApplicationEventBus EventHandler eventListener) {
    messageEventListener = requireNonNull(eventListener, "eventListener");
    cutBackCount = capacity;
  }

  /**
   * Returns this buffer's capacity.
   *
   * @return This buffer's capacity.
   */
  public int getCapacity() {
    LOG.debug("method entry");
    return capacity;
  }

  /**
   * Adjusts this buffer's <code>capacity</code>.
   * If the new capacity is less than this buffer's <code>cut back count</code>,
   * the latter is set to the new capacity as well.
   * If the new capacity is less than the current number of messages in this
   * buffer, messages are removed until the number of messages equals the
   * buffer's <code>cut back count</code>.
   *
   * @param newCapacity The buffer's new capacity. Must be at least 1.
   * @throws IllegalArgumentException If <code>newCapacity</code> is less than
   * 1.
   */
  public void setCapacity(int newCapacity) {
    LOG.debug("method entry");
    if (newCapacity < 1) {
      throw new IllegalArgumentException("newCapacity must be at least 1");
    }
    capacity = newCapacity;
    if (cutBackCount > capacity) {
      cutBackCount = capacity;
    }
    cutBackMessages();
  }

  /**
   * Returns this buffer's <code>cut back count</code>.
   *
   * @return This buffer's <code>cut back count</code>.
   */
  public int getCutBackCount() {
    LOG.debug("method entry");
    return cutBackCount;
  }

  /**
   * Sets this buffer's new <code>cut back count</code>.
   *
   * @param newValue This buffer's new <code>cut back count</code>. Must be
   * greater than 0 and less than or equal to this buffer's
   * <code>capacity</code>.
   */
  public void setCutBackCount(int newValue) {
    LOG.debug("method entry");
    if (newValue < 0 || newValue > capacity) {
      throw new IllegalArgumentException(
          "newValue must be greater than 0 and less than or equal to capacity");
    }
    cutBackCount = newValue;
  }

  /**
   * Returns the number of messages currently in this buffer.
   *
   * @return The number of messages currently in this buffer.
   */
  public int getMessageCount() {
    LOG.debug("method entry");
    return notifications.size();
  }

  /**
   * Adds a notification to the buffer.
   *
   * @param notification The notification to be added.
   */
  public void addNotification(UserNotification notification) {
    requireNonNull(notification, "notification");

    notifications.add(notification);
    LOG.debug("New notification added: {}", notification.getText());
    // Make sure we don't have too many messages now.
    cutBackMessages();
    // Emit an event for this message.
    emitMessageEvent(notification);
  }

  /**
   * Returns all messages.
   *
   * @return A list of all existing Message objects.
   */
  public List<UserNotification> getNotifications() {
    LOG.debug("method entry");
    return new LinkedList<>(notifications);
  }

  /**
   * Returns all notifications that are accepted by the given filter, or all notifications, if no
   * filter is given.
   *
   * @param predicate The predicate used to filter. May be <code>null</code>.
   * @return A list of notifications accepted by the given filter.
   */
  public List<UserNotification> getNotifications(Predicate<UserNotification> predicate) {
    LOG.debug("method entry");
    List<UserNotification> result = new LinkedList<>();
    for (UserNotification notification : notifications) {
      if (predicate == null || predicate.test(notification)) {
        result.add(notification);
      }
    }
    return result;
  }

  /**
   * Removes all messages from this buffer.
   */
  public void clear() {
    LOG.debug("method entry");
    notifications.clear();
  }

  /**
   * Removes messages until we're down to this buffer's <code>cut back count</code>.
   */
  private void cutBackMessages() {
    LOG.debug("method entry");
    if (notifications.size() > capacity) {
      // Cut back number of messages.
      while (notifications.size() > cutBackCount) {
        notifications.remove(notifications.size() - 1);
      }
    }
  }

  /**
   * Emits an event for the given message.
   *
   * @param message The message to emit an event for.
   */
  public void emitMessageEvent(UserNotification message) {
    @SuppressWarnings("deprecation")
    org.opentcs.access.TCSNotificationEvent event
        = new org.opentcs.access.TCSNotificationEvent(message);
    messageEventListener.onEvent(event);
    messageEventListener.onEvent(new NotificationPublicationEvent(message));
  }
}
