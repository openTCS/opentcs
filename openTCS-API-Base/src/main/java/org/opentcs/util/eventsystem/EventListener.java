/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.eventsystem;

import javax.annotation.Nonnull;

/**
 * This interfaces declares the method that is called to notify an object of an
 * event that has happened.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual event implementation.
 */
public interface EventListener<E extends Event> {

  /**
   * Called when an event has happened.
   *
   * @param event The event that has happened.
   */
  void processEvent(@Nonnull E event);
}
