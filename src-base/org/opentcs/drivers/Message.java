/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers;

import java.io.Serializable;
import java.util.Comparator;
import org.opentcs.util.UniqueTimestampGenerator;

/**
 * Instances of this class represent timestamped messages, usually meant to be
 * read by a user.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Message
implements Serializable, Cloneable {
  /**
   * A <code>Comparator</code> for sorting messages chronologically.
   */
  public static final Comparator<Message> youngestToEldestComparator =
        new YoungestToEldestComparator();
  /**
   * A <code>Comparator</code> for sorting messages by their type.
   */
  public static final Comparator<Message> typeComparator = new TypeComparator();
  /**
   * The timestamp generator for message creation times.
   */
  private static final UniqueTimestampGenerator timestampGenerator =
        new UniqueTimestampGenerator();
  
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
    if (messageText == null) {
      throw new NullPointerException("messageText is null");
    }
    if (messageType == null) {
      throw new NullPointerException("messageType is null");
    }
    message = messageText;
    type = messageType;
    timestamp = timestampGenerator.getNextTimestamp();
  }
  
  // Methods not declared in any interface start here
  
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
  public boolean equals(Object o) {
    if (o instanceof Message) {
      Message other = (Message) o;
      return other.timestamp == this.timestamp &&
            this.message.equals(other.message) &&
            this.type.equals(other.type);
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return (int) timestamp ^ message.hashCode() ^ type.hashCode();
  }

  @Override
  public Message clone() {
    Message clone;
    try {
      clone = (Message) super.clone();
    }
    catch (CloneNotSupportedException exc) {
      throw new IllegalStateException("Unexpected exception", exc);
    }
    return clone;
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
  
  /**
   * A comparator for sorting messages chronologically.
   */
  private static class YoungestToEldestComparator
  implements Comparator<Message> {
    /**
     * Creates a new EldestMessagesLastComparator.
     */
    public YoungestToEldestComparator() {
    }

    @Override
    public int compare(Message o1, Message o2) {
      int result;
      long difference = o1.getTimestamp() - o2.getTimestamp();
      if (difference < 0) {
        result = -1;
      }
      else if (difference > 0) {
        result = 1;
      }
      else {
        result = 0;
      }
      return result;
    }
  }
  
  /**
   * A comparator for sorting messages by their type.
   */
  private static class TypeComparator
  implements Comparator<Message> {
    /**
     * Creates a new TypeComparator.
     */
    public TypeComparator() {
    }

    @Override
    public int compare(Message o1, Message o2) {
      int result = o1.type.compareTo(o2.type);
      if(result == 0){
        result = Message.youngestToEldestComparator.compare(o1,o2);   
      }
      return result;
    }
  }
}
