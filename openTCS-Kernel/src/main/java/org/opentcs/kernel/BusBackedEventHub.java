/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import static java.util.Objects.requireNonNull;
import org.opentcs.components.Lifecycle;
import org.opentcs.util.annotations.ScheduledApiChange;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;

/**
 * An event hub implementation for TCSEvents that delivers events via an event
 * bus and forwards events received to attached listeners.
 * <p>
 * Registered listeners will be forwarded TCSEvent instances this event hub
 * receives via the event bus it is subscribed to.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual event implementation.
 * @deprecated As deprecated as SynchronousEventHub.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public class BusBackedEventHub<E extends org.opentcs.util.eventsystem.Event>
    extends org.opentcs.util.eventsystem.SynchronousEventHub<E>
    implements EventHandler,
               Lifecycle {

  /**
   * The event bus.
   */
  private final EventBus eventBus;
  /**
   * The class of objects to be dispatched.
   */
  private final Class<E> clazz;
  /**
   * Whether this event hub is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance using the given event bus.
   *
   * @param eventBus The event bus.
   * @param clazz The class of objects to be dispatched. This is required to
   * work around the Java compiler's erasure colliding with the event bus
   * inspecting {@link #onEvent(java.lang.Object)}'s parameters at
   * runtime.
   */
  public BusBackedEventHub(EventBus eventBus, Class<E> clazz) {
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.clazz = requireNonNull(clazz, "clazz");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    eventBus.subscribe(this);
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    eventBus.unsubscribe(this);
  }

  @Override
  public void processEvent(E event) {
    // We just forward to the event bus here to publish to listeners registered
    // to this hub and to the event bus.
    eventBus.onEvent(event);
  }

  @Override
  public void onEvent(Object event) {
    if (!clazz.isAssignableFrom(event.getClass())) {
      return;
    }
    // Dispatch the event to the listeners registered with this hub.
    super.processEvent(clazz.cast(event));
  }
}
