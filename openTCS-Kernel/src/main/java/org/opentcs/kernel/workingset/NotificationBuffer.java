/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.NotificationPublicationEvent;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.notification.UserNotification;
import static org.opentcs.util.Assertions.checkInRange;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A buffer which can store a (configurable) limited number of
 * {@link UserNotification UserNotification} objects.
 * <p>
 * When a new message is added to the buffer and the number of messages in the buffer exceeds its
 * <code>capacity</code>, messages are removed from the buffer until it contains not more than
 * <code>capacity</code>.
 * </p>
 * <p>
 * Note that no synchronization is done inside this class. Concurrent access of
 * instances of this class must be synchronized externally.
 * </p>
 */
public class NotificationBuffer {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(NotificationBuffer.class);
  /**
   * The actual messages.
   */
  private final Queue<UserNotification> notifications = new ArrayDeque<>();
  /**
   * The maximum number of messages that should be kept in this buffer.
   */
  private int capacity = 500;
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
  }

  /**
   * Returns this buffer's capacity.
   *
   * @return This buffer's capacity.
   */
  public int getCapacity() {
    return capacity;
  }

  /**
   * Adjusts this buffer's <code>capacity</code>.
   * If the new capacity is less than the current number of messages in this
   * buffer, messages are removed until the number of messages equals the
   * buffer's <code>capacity</code>.
   *
   * @param capacity The buffer's new capacity. Must be at least 1.
   * @throws IllegalArgumentException If <code>newCapacity</code> is less than 1.
   */
  public void setCapacity(int capacity) {
    this.capacity = checkInRange(capacity, 1, Integer.MAX_VALUE, "capacity");
    cutBackMessages();
  }

  /**
   * Adds a notification to the buffer.
   *
   * @param notification The notification to be added.
   */
  public void addNotification(UserNotification notification) {
    requireNonNull(notification, "notification");

    notifications.add(notification);
    LOG.info("User notification added: {}", notification);

    // Make sure we don't have too many messages now.
    cutBackMessages();
    // Emit an event for this message.
    emitMessageEvent(notification);
  }

  /**
   * Returns all notifications that are accepted by the given filter, or all notifications, if no
   * filter is given.
   *
   * @param predicate The predicate used to filter. May be <code>null</code> to return all
   * notifications.
   * @return A list of notifications accepted by the given filter.
   */
  public List<UserNotification> getNotifications(@Nullable Predicate<UserNotification> predicate) {
    Predicate<UserNotification> filterPredicate
        = predicate == null
            ? (notification) -> true
            : predicate;

    return notifications.stream()
        .filter(filterPredicate)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Removes all messages from this buffer.
   */
  public void clear() {
    notifications.clear();
  }

  /**
   * Emits an event for the given message.
   *
   * @param message The message to emit an event for.
   */
  public void emitMessageEvent(UserNotification message) {
    messageEventListener.onEvent(new NotificationPublicationEvent(message));
  }

  private void cutBackMessages() {
    while (notifications.size() > capacity) {
      notifications.remove();
    }
  }
}
