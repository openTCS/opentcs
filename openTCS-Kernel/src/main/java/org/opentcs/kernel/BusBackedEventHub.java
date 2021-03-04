/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;
import org.opentcs.util.eventsystem.Event;
import org.opentcs.util.eventsystem.SynchronousEventHub;

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
 */
public class BusBackedEventHub<E extends Event>
    extends SynchronousEventHub<E> {

  /**
   * The event bus.
   */
  private final MBassador<Object> eventBus;
  /**
   * The class of objects to be dispatched.
   */
  private final Class<E> clazz;

  /**
   * Creates a new instance using the given event bus.
   *
   * @param eventBus The event bus.
   * @param clazz The class of objects to be dispatched. This is required to
   * work around the Java compiler's erasure colliding with the event bus
   * inspecting {@link #handleBusEvent(java.lang.Object)}'s parameters at
   * runtime.
   */
  @Inject
  public BusBackedEventHub(MBassador<Object> eventBus, Class<E> clazz) {
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.clazz = requireNonNull(clazz, "clazz");
  }

  @Override
  public void processEvent(E event) {
    // We just forward to the event bus here to publish to listeners registered
    // to this hub and to the event bus.
    eventBus.publish(event);
  }

  @Handler
  public void handleBusEvent(Object event) {
    if (!clazz.isAssignableFrom(event.getClass())) {
      return;
    }
    // Dispatch the event to the listeners registered with this hub.
    super.processEvent(clazz.cast(event));
  }
}
