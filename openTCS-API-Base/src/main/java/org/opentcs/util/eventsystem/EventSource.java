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
 * This interface declares methods for registering and unregistering event
 * listeners with a source of events.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual event implementation.
 * @deprecated For event handling, use classes in <code>org.opentcs.util.event</code> instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public interface EventSource<E extends Event> {

  /**
   * Registers an event listener.
   *
   * @param listener The event listener to add. If the listener is already
   * registered, it is re-registered with the given event filter.
   * @param filter The filter deciding which events to deliver to the given
   * listener.
   * @deprecated Use {@link #addEventListener(org.opentcs.util.eventsystem.EventListener)} instead,
   * and filter out unwanted events in
   * {@link EventListener#processEvent(org.opentcs.util.eventsystem.Event)}. (Note that you should
   * do that, anyway, which is why a separate event filter is superfluous.)
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  void addEventListener(EventListener<E> listener, EventFilter<E> filter);

  /**
   * Registers an event listener.
   *
   * @param listener The event listener to add.
   */
  void addEventListener(EventListener<E> listener);

  /**
   * Unregisters an event listener.
   *
   * @param listener The listener to remove. If no such listener is registered,
   * nothing happens.
   */
  void removeEventListener(EventListener<E> listener);
}
