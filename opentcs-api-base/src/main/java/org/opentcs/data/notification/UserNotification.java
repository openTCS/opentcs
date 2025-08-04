// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.notification;

import jakarta.annotation.Nullable;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A notification to be read by a user.
 */
public class UserNotification
    implements
      Serializable {

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
  public UserNotification(
      @Nullable
      String source,
      String text,
      Level level
  ) {
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
    return Objects.hash(text, level, timestamp);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof UserNotification other) {
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
