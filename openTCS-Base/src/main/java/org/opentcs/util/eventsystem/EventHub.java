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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An <code>EventHub</code> dispatches events to registered listeners.
 * A client, i.e. an <code>EventListener</code>, may register with an
 * <code>EventHub</code> for receiving events. The <code>EventHub</code> will
 * forward any events received to the client either synchronously (i.e. in the
 * context of the thread calling {@link #processEvent processEvent()}) or
 * asynchronously (i.e. in a separate thread).
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual event implementation.
 */
public abstract class EventHub<E extends Event>
    implements EventListener<E>,
               EventSource<E> {

  /**
   * The registered event listeners.
   */
  private final Map<EventListener<E>, EventFilter<E>> eventListeners
      = new ConcurrentHashMap<>();

  /**
   * Creates a new instance.
   */
  protected EventHub() {
    // Do nada.
  }

  @Override
  public void addEventListener(EventListener<E> listener,
                               EventFilter<E> filter) {
    Objects.requireNonNull(listener, "listener is null");
    Objects.requireNonNull(filter, "filter is null");
    eventListeners.put(listener, filter);
  }

  @Override
  public void removeEventListener(EventListener<E> listener) {
    Objects.requireNonNull(listener, "listener is null");
    eventListeners.remove(listener);
  }

  /**
   * Returns a map of registered event listeners.
   *
   * @return A map of registered event listeners.
   */
  protected Map<EventListener<E>, EventFilter<E>> getEventListeners() {
    return eventListeners;
  }
}
