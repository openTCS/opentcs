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
 * Implementations of this interface filter events and can be used to prevent
 * unwanted events from reaching <code>EventListener</code>s.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual event implementation.
 */
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
