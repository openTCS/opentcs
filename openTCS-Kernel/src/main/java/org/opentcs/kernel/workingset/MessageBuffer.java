/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.opentcs.access.TCSMessageEvent;
import org.opentcs.data.message.Message;
import org.opentcs.util.eventsystem.CentralEventHub;
import org.opentcs.util.eventsystem.DummyEventListener;
import org.opentcs.util.eventsystem.EventListener;
import org.opentcs.util.eventsystem.TCSEvent;

/**
 * A buffer which can store a (configurable) limited number of
 * {@link Message Message} objects.
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
public class MessageBuffer {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(MessageBuffer.class.getName());
  /**
   * The actual messages.
   */
  private final List<Message> messages = new LinkedList<>();
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
  private final EventListener<TCSEvent> messageEventListener;

  /**
   * Creates a new MessageBuffer.
   */
  public MessageBuffer() {
    this(new DummyEventListener<TCSEvent>());
  }
  
  /**
   * Creates a new instance that uses the given event listener.
   *
   * @param eventListener The event listener to be used.
   */
  @Inject
  public MessageBuffer(@CentralEventHub EventListener<TCSEvent> eventListener) {
    messageEventListener = requireNonNull(eventListener, "eventListener");
    cutBackCount = capacity;
  }

  /**
   * Returns this buffer's capacity.
   *
   * @return This buffer's capacity.
   */
  public int getCapacity() {
    log.finer("method entry");
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
    log.finer("method entry");
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
    log.finer("method entry");
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
    log.finer("method entry");
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
    log.finer("method entry");
    return messages.size();
  }

  /**
   * Adds a message to the buffer.
   *
   * @param msgText The new message's text.
   * @param msgType The new message's type.
   * @return The newly created message object.
   */
  public Message createMessage(String msgText, Message.Type msgType) {
    log.finer("method entry");
    // Create an instance.
    Message newMessage = new Message(msgText, msgType);
    messages.add(newMessage);
    log.fine("New message added: " + newMessage.getMessage());
    // Make sure we don't have too many messages now.
    cutBackMessages();
    // Emit an event for this message.
    emitMessageEvent(newMessage);
    // Return the newly created message.
    return newMessage;
  }

  /**
   * Returns all messages.
   *
   * @return A list of all existing Message objects.
   */
  public List<Message> getMessages() {
    log.finer("method entry");
    return new LinkedList<>(messages);
  }

  /**
   * Returns all messages whose timestamp is greater than or equal to a given
   * one, i.e. all messages created after the given point of time.
   *
   * @param minTimestamp The timestamp that returned messages' timestamps may
   * not precede.
   * @return A set of messages that were all created after the given point of
   * time.
   */
  public List<Message> getMessages(long minTimestamp) {
    log.finer("method entry");
    List<Message> result = new LinkedList<>();
    for (Message message : messages) {
      if (message.getTimestamp() >= minTimestamp) {
        result.add(message);
      }
    }
    return result;
  }

  /**
   * Removes all messages from this buffer.
   */
  public void clear() {
    log.finer("method entry");
    messages.clear();
  }

  /**
   * Removes messages until we're down to this buffer's <code>cut back
   * count</code>.
   */
  private void cutBackMessages() {
    log.finer("method entry");
    if (messages.size() > capacity) {
      // Cut back number of messages.
      while (messages.size() > cutBackCount) {
        messages.remove(messages.size() - 1);
      }
    }
  }
  /**
   * Emits an event for the given message.
   *
   * @param message The message to emit an event for.
   */
  public void emitMessageEvent(Message message) {
    TCSMessageEvent event = new TCSMessageEvent(message);
    messageEventListener.processEvent(event);
  }
}
