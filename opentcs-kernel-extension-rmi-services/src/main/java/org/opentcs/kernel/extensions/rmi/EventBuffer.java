// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.rmi;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkArgument;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.util.event.EventHandler;

/**
 * Stores events and keeps them until a client fetches them.
 */
public class EventBuffer
    implements
      EventHandler {

  /**
   * The buffered events.
   */
  private final Deque<Object> events = new LinkedList<>();
  /**
   * This buffer's event filter.
   */
  private Predicate<Object> eventFilter;
  /**
   * A flag indicating whether this event buffer's client is currently waiting for an event.
   */
  private boolean waitingClient;

  /**
   * Creates a new instance
   *
   * @param eventFilter This buffer's initial event filter.
   */
  public EventBuffer(
      @Nonnull
      Predicate<Object> eventFilter
  ) {
    this.eventFilter = requireNonNull(eventFilter, "eventFilter");
  }

  // Methods declared in interface EventListener start here
  @Override
  public void onEvent(Object event) {
    requireNonNull(event, "event");
    synchronized (events) {
      if (eventFilter.test(event)) {
        if (!tryMergeWithPreviousEvent(event)) {
          events.add(event);
        }

        // If the client is waiting for an event, wake it up, since there is one now.
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
  public List<Object> getEvents(long timeout)
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
      List<Object> result = new ArrayList<>(events);
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
  public void setEventFilter(
      @Nonnull
      Predicate<Object> eventFilter
  ) {
    synchronized (events) {
      this.eventFilter = requireNonNull(eventFilter);
    }
  }

  /**
   * If possible, merge the given new event with the previous one in the buffer.
   *
   * @param event The new event.
   * @return <code>true</code> if the new event was merged with the previous one.
   */
  private boolean tryMergeWithPreviousEvent(Object event) {
    if (!(event instanceof TCSObjectEvent currentEvent)
        || !(events.peekLast() instanceof TCSObjectEvent previousEvent)) {
      return false;
    }

    if (currentEvent.getType() != TCSObjectEvent.Type.OBJECT_MODIFIED
        || previousEvent.getType() != TCSObjectEvent.Type.OBJECT_MODIFIED) {
      return false;
    }

    if (!Objects.equals(
        currentEvent.getCurrentObjectState().getReference(),
        previousEvent.getCurrentObjectState().getReference()
    )) {
      return false;
    }

    events.removeLast();
    events.add(
        new TCSObjectEvent(
            currentEvent.getCurrentObjectState(),
            previousEvent.getPreviousObjectState(),
            TCSObjectEvent.Type.OBJECT_MODIFIED
        )
    );

    return true;
  }
}
