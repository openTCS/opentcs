// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.event;

/**
 * A source of events that can be subscribed to.
 */
public interface EventSource {

  /**
   * Subscribes the given listener to events emitted by this source.
   *
   * @param listener The listener to be subscribed.
   */
  void subscribe(EventHandler listener);

  /**
   * Unsubscribes the given listener.
   *
   * @param listener The listener to be unsubscribed.
   */
  void unsubscribe(EventHandler listener);
}
