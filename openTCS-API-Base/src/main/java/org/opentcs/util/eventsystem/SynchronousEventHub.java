/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.eventsystem;

import java.util.HashMap;
import java.util.Map;
import org.opentcs.util.annotations.ScheduledApiChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An <code>EventHub</code> implementation that dispatches events synchronously,
 * i.e. in the context of the thread executing <code>processEvent()</code>.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual event implementation.
 * @deprecated For event handling, use classes in <code>org.opentcs.util.event</code> instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public class SynchronousEventHub<E extends Event>
    extends EventHub<E> {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(SynchronousEventHub.class);

  /**
   * Creates a new SynchronousEventHub.
   */
  public SynchronousEventHub() {
  }

  @Override
  @SuppressWarnings("deprecation")
  public void processEvent(E event) {
    for (Map.Entry<EventListener<E>, EventFilter<E>> curEntry
             : new HashMap<>(getEventListeners()).entrySet()) {
      try {
        if (curEntry.getValue().accept(event)) {
          curEntry.getKey().processEvent(event);
        }
      }
      catch (Exception exc) {
        LOG.warn("Exception thrown by event handler", exc);
      }
    }
  }
}
