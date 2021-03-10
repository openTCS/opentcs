/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.event;

/**
 * A source of events that can be subscribed to.
 *
 * @author Stefan Walter (Fraunhofer IML)
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
