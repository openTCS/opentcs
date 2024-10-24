// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.event;

/**
 * A distributor of events.
 * Forwards events received via {@link #onEvent(java.lang.Object)} to all subscribed handlers.
 */
public interface EventBus
    extends
      EventHandler,
      EventSource {

}
