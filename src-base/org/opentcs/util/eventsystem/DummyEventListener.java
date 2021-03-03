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
 * An <code>EventListener</code> that does not do anything when an event
 * arrives.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual event implementation.
 */
public class DummyEventListener<E extends Event>
implements EventListener<E> {
  /**
   * Creates a new DummyEventListener.
   */
  public DummyEventListener() {
  }
  
  /**
   * Does nothing.
   *
   * @param event The event.
   */
  @Override
  public void processEvent(E event) {
    // Do nada.
  }
}
