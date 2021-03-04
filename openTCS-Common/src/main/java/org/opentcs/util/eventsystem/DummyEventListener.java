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
 * An <code>EventListener</code> that does not do anything when an event
 * arrives.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual event implementation.
 * @deprecated Will be removed.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
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
