/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.notification;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * A notification to be read by a user.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class UserNotification
    implements Serializable {

  /**
   * An identifier of the notification's source.
   */
  @Nullable
  private final String source;
  /**
   * This message's text.
   */
  private final String text;
  /**
   * This message's type.
   */
  private final Level level;
  /**
   * This message's creation timestamp.
   */
  private final Instant timestamp;

  /**
   * Creates a new Message.
   *
   * @param source An identifier of the notification's source.
   * @param text The actual message text.
   * @param level The new message's level.
   */
  public UserNotification(@Nullable String source, String text, Level level) {
    this.source = source;
    this.text = Objects.requireNonNull(text, "text");
    this.level = Objects.requireNonNull(level, "level");
    this.timestamp = Instant.now();
  }

  /**
   * Creates a new Message.
   *
   * @param text The actual message text.
   * @param level The new message's level.
   */
  public UserNotification(String text, Level level) {
    this(null, text, level);
  }

  /**
   * Returns this notification's (optional) source.
   *
   * @return This notification's (optional) source.
   */
  @Nullable
  public String getSource() {
    return source;
  }

  /**
   * Returns this message's text.
   *
   * @return This message's text.
   */
  public String getText() {
    return text;
  }

  /**
   * Returns this message's type.
   *
   * @return This message's type.
   */
  public Level getLevel() {
    return level;
  }

  /**
   * Returns this message's creation timestamp.
   *
   * @return This message's creation timestamp.
   */
  public Instant getTimestamp() {
    return timestamp;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 89 * hash + Objects.hashCode(this.text);
    hash = 89 * hash + Objects.hashCode(this.level);
    hash = 89 * hash + Objects.hashCode(this.timestamp);
    return hash;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof UserNotification) {
      UserNotification other = (UserNotification) o;
      return Objects.equals(this.source, other.source)
          && Objects.equals(this.text, other.text)
          && Objects.equals(this.level, other.level)
          && Objects.equals(this.timestamp, other.timestamp);
    }
    return false;
  }

  @Override
  public String toString() {
    return "UserNotification{"
        + "source=" + source
        + ", timestamp=" + timestamp
        + ", level=" + level
        + ", text=" + text
        + '}';
  }

  /**
   * Defines the possible message types.
   */
  public enum Level {

    /**
     * Marks usual, informational content.
     */
    INFORMATIONAL,
    /**
     * Marks unusual content a user would probably be interested in.
     */
    NOTEWORTHY,
    /**
     * Marks important content the user should not miss.
     */
    IMPORTANT
  }
}
