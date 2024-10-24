// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.event;

/**
 * A handler for events emitted by an {@link EventSource}.
 */
public interface EventHandler {

  /**
   * Processes the event object.
   *
   * @param event The event object.
   */
  void onEvent(Object event);
}
