// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common.mqtt;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nonnull;

/**
 * A message received via a topic.
 */
public class IncomingMessage {

  /**
   * The topic this message was received on.
   */
  private final String topic;
  /**
   * The message content.
   */
  private final String message;

  /**
   * Creates a new instance.
   *
   * @param topic The topic the message was received on.
   * @param message The message.
   */
  public IncomingMessage(
      @Nonnull
      String topic,
      @Nonnull
      String message
  ) {
    this.topic = requireNonNull(topic, "topic");
    this.message = requireNonNull(message, "message");
  }

  /**
   * Returns the topic the message was received on.
   *
   * @return the topic the message was received on
   */
  public String getTopic() {
    return topic;
  }

  /**
   * Returns the message.
   *
   * @return the message
   */
  public String getMessage() {
    return message;
  }
}
