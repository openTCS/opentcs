/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.eventsystem;

import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Implementations of this interface filter events and can be used to prevent
 * unwanted events from reaching <code>EventListener</code>s.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual event implementation.
 * @deprecated {@link EventSource#addEventListener(org.opentcs.util.eventsystem.EventListener, org.opentcs.util.eventsystem.EventFilter)}
 * will be removed.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public interface EventFilter<E extends Event> {

  /**
   * Checks if this filter accepts a given event.
   *
   * @param event The event to check.
   * @return <code>true</code> if this filter accepts the event, else
   * <code>false</code>.
   */
  boolean accept(E event);
}
