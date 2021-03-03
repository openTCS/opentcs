/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.message;

import java.io.Serializable;
import java.util.Objects;
import org.opentcs.util.UniqueTimestampGenerator;

/**
 * Instances of this class represent messages, usually meant to be read by a
 * user.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class Message
    implements Serializable, Cloneable {

  /**
   * The timestamp generator for message creation times.
   */
  private static final UniqueTimestampGenerator timestampGenerator
      = new UniqueTimestampGenerator();
  /**
   * This message's text.
   */
  private final String message;
  /**
   * This message's type.
   */
  private final Type type;
  /**
   * This message's creation timestamp.
   */
  private final long timestamp;

  /**
   * Creates a new Message.
   *
   * @param messageText The actual message text.
   * @param messageType The new message's type.
   */
  public Message(String messageText, Type messageType) {
    message = Objects.requireNonNull(messageText, "messageText is null");
    type = Objects.requireNonNull(messageType, "messageType is null");
    timestamp = timestampGenerator.getNextTimestamp();
  }

  /**
   * Returns this message's text.
   *
   * @return This message's text.
   */
  public String getMessage() {
    return message;
  }

  /**
   * Returns this message's type.
   *
   * @return This message's type.
   */
  public Type getType() {
    return type;
  }

  /**
   * Returns this message's creation timestamp.
   *
   * @return This message's creation timestamp.
   */
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public Message clone() {
    try {
      Message clone = (Message) super.clone();
      return clone;
    }
    catch (CloneNotSupportedException exc) {
      throw new RuntimeException("Unexpected exception", exc);
    }
  }

  @Override
  public String toString() {
    return "Message(" + type + ": " + message + ")";
  }

  /**
   * Defines the possible message types.
   */
  public enum Type {

    /**
     * Marks an informational message.
     */
    INFO,
    /**
     * Marks a warning message.
     */
    WARNING,
    /**
     * Marks an error message.
     */
    ERROR
  }
}
