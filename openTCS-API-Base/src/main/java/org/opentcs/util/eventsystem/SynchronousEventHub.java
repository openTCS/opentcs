/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.eventsystem;

import java.util.Map;

/**
 * An <code>EventHub</code> implementation that dispatches events synchronously,
 * i.e. in the context of the thread executing <code>processEvent()</code>.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual event implementation.
 */
public class SynchronousEventHub<E extends Event>
    extends EventHub<E> {

  /**
   * Creates a new SynchronousEventHub.
   */
  public SynchronousEventHub() {
  }

  @Override
  public void processEvent(E event) {
    for (Map.Entry<EventListener<E>, EventFilter<E>> curEntry
         : getEventListeners().entrySet()) {
      if (curEntry.getValue().accept(event)) {
        curEntry.getKey().processEvent(event);
      }
    }
  }
}
