/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.eventsystem;

import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import static org.opentcs.util.Assertions.checkArgument;

/**
 * Stores events and keeps them until a client fetches them.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual event implementation.
 */
public class EventBuffer<E extends Event>
    implements EventListener<E> {

  /**
   * The buffered events.
   */
  private final List<E> events = new LinkedList<>();
  /**
   * This buffer's event filter.
   */
  private EventFilter<E> filter;
  /**
   * A flag indicating whether this event buffer's client is currently waiting
   * for an event.
   */
  private boolean waitingClient;

  /**
   * Creates a new EventBuffer.
   *
   * @param eventFilter This buffer's initial event filter.
   */
  public EventBuffer(@Nonnull EventFilter<E> eventFilter) {
    filter = requireNonNull(eventFilter, "eventFilter");
  }

  // Methods declared in interface EventListener start here
  @Override
  public void processEvent(E event) {
    requireNonNull(event, "event");
    synchronized (events) {
      if (filter.accept(event)) {
        events.add(event);
        // If the client is waiting for an event, wake it up, since there is one
        // now.
        if (waitingClient) {
          events.notify();
        }
      }
    }
  }

  // Methods not declared in any interface start here
  /**
   * Returns a list of events that are currently stored in this buffer and
   * clears the buffer.
   * If the buffer is currently empty, block until an event arrives, or for the
   * specified amount of time to pass, whichever occurs first.
   *
   * @param timeout The maximum amount of time (in ms) to wait for an event to
   * arrive. Must be at least 0 (in which case this method will return
   * immediately, without waiting for an event to arrive).
   * @return A list of events that are currently stored in this buffer.
   * @throws IllegalArgumentException If <code>timeout</code> is less than 0.
   */
  public List<E> getEvents(long timeout)
      throws IllegalArgumentException {
    checkArgument(timeout >= 0, "timeout < 0: %s", timeout);
    synchronized (events) {
      if (timeout > 0 && events.isEmpty()) {
        waitingClient = true;
        try {
          events.wait(timeout);
        }
        catch (InterruptedException exc) {
          throw new IllegalStateException("Unexpectedly interrupted", exc);
        }
        finally {
          waitingClient = false;
        }
      }
      List<E> result = new LinkedList<>(events);
      events.clear();
      return result;
    }
  }

  /**
   * Checks whether a client is currently waiting for events arriving in this
   * buffer.
   *
   * @return <code>true</code> if a client is currently waiting, else
   * <code>false</code>.
   */
  public boolean hasWaitingClient() {
    synchronized (events) {
      return waitingClient;
    }
  }

  /**
   * Sets this buffer's event filter.
   *
   * @param eventFilter This buffer's new event filter.
   */
  public void setFilter(@Nonnull EventFilter<E> eventFilter) {
    synchronized (events) {
      filter = requireNonNull(eventFilter, "eventFilter");
    }
  }
}
