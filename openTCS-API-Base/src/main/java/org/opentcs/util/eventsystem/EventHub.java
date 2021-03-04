/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.eventsystem;

import java.util.Collection;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.ConcurrentHashMap;
import org.opentcs.util.annotations.ScheduledApiChange;

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
 * @deprecated For event handling, use classes in <code>org.opentcs.util.event</code> instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public abstract class EventHub<E extends Event>
    implements EventListener<E>,
               EventSource<E> {

  /**
   * The registered event listeners.
   */
  @SuppressWarnings("deprecation")
  private final Map<EventListener<E>, EventFilter<E>> eventListeners = new ConcurrentHashMap<>();

  /**
   * Creates a new instance.
   */
  protected EventHub() {
  }

  @Override
  @Deprecated
  public void addEventListener(EventListener<E> listener, EventFilter<E> filter) {
    requireNonNull(listener, "listener");
    requireNonNull(filter, "filter");
    eventListeners.put(listener, filter);
  }

  @Override
  public void addEventListener(EventListener<E> listener) {
    requireNonNull(listener, "listener");
    eventListeners.put(listener, (event) -> true);
  }

  @Override
  public void removeEventListener(EventListener<E> listener) {
    requireNonNull(listener, "listener");
    eventListeners.remove(listener);
  }

  /**
   * Returns a map of registered event listeners.
   *
   * @return A map of registered event listeners.
   * @deprecated Use {@link #eventListeners()} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  protected Map<EventListener<E>, EventFilter<E>> getEventListeners() {
    return eventListeners;
  }

  /**
   * Returns a collection of registered event listeners.
   *
   * @return A collection of registered event listeners.
   */
  protected Collection<EventListener<E>> eventListeners() {
    return eventListeners.keySet();
  }
}
