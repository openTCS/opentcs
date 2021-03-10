/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.event;

/**
 * A distributor of events.
 * Forwards events received via {@link #onEvent(java.lang.Object)} to all subscribed handlers.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface EventBus
    extends EventHandler,
            EventSource {

}
