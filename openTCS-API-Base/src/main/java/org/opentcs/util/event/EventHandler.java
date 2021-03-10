/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.event;

/**
 * A handler for events emitted by an {@link EventSource}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface EventHandler {

  /**
   * Processes the event object.
   *
   * @param event The event object.
   */
  void onEvent(Object event);
}
