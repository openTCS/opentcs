/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.eventsystem;

/**
 * This interface declares methods for registering and unregistering event
 * listeners with a source of events.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual event implementation.
 */
public interface EventSource<E extends Event> {
  /**
   * Registers an event listener.
   *
   * @param listener The event listener to add. If the listener is already
   * registered, it is re-registered with the given event filter.
   * @param filter The filter deciding which events to deliver to the given
   * listener.
   */
  void addEventListener(EventListener<E> listener,
        EventFilter<E> filter);
  
  /**
   * Unregisters an event listener.
   *
   * @param listener The listener to remove. If no such listener is registered,
   * nothing happens.
   */
  void removeEventListener(EventListener<E> listener);
}
