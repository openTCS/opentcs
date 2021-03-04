/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.eventsystem;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import org.opentcs.util.annotations.ScheduledApiChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An <code>EventHub</code> implementation that dispatches events
 * asynchronously, i.e. in a separate thread.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual event implementation.
 * @deprecated For event handling, use classes in <code>org.opentcs.util.event</code> instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public class AsynchronousEventHub<E extends Event>
    extends EventHub<E> {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AsynchronousEventHub.class);
  /**
   * The received events, in chronological order.
   */
  private final Queue<E> incomingEvents = new LinkedList<>();
  /**
   * The task doing the dispatching of events in the asynchronous case.
   */
  private final DispatcherTask dispatcherTask;
  /**
   * The thread in which the DispatcherTask runs.
   */
  private final Thread dispatcherThread;

  /**
   * Creates a new SynchronousEventHub.
   */
  public AsynchronousEventHub() {
    dispatcherTask = new DispatcherTask();
    dispatcherThread = new Thread(dispatcherTask);
    dispatcherThread.setPriority(Thread.MIN_PRIORITY);
    dispatcherThread.start();
  }

  @Override
  public void processEvent(E event) {
    synchronized (incomingEvents) {
      // Add the event to the inbox and notify the dispatcher.
      incomingEvents.add(event);
      incomingEvents.notify();
    }
  }

  /**
   * Instances of this class wait for events to arrive and forward them to
   * interested clients.
   */
  private class DispatcherTask
      implements Runnable {

    /**
     * A flag indicating whether this task is terminated.
     */
    private volatile boolean terminated;

    /**
     * Creates a new DispatcherTask.
     */
    public DispatcherTask() {
    }

    /**
     * Terminates this task.
     */
    public void terminate() {
      terminated = true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void run() {
      Queue<E> outgoingEvents = new LinkedList<>();
      while (!terminated) {
        synchronized (incomingEvents) {
          // Wait until an event has arrived.
          while (incomingEvents.isEmpty()) {
            try {
              incomingEvents.wait();
            }
            catch (InterruptedException exc) {
              // We shouldn't be interrupted by anyone.
              throw new IllegalStateException("Unexpectedly interrupted", exc);
            }
          }
          // Copy the events to a local list and clear the inbox so we can go on
          // without blocking other threads.
          outgoingEvents.addAll(incomingEvents);
          incomingEvents.clear();
        }
        Iterator<E> eventIter = outgoingEvents.iterator();
        while (eventIter.hasNext()) {
          E curEvent = eventIter.next();
          // Dispatch the event to all listeners whose filter accepts it.
          for (Map.Entry<EventListener<E>, EventFilter<E>> curEntry
                   : getEventListeners().entrySet()) {
            try {
              if (curEntry.getValue().accept(curEvent)) {
                curEntry.getKey().processEvent(curEvent);
              }
            }
            catch (Exception exc) {
              LOG.warn("Exception thrown by event handler", exc);
            }
          }
          eventIter.remove();
        }
      }
    }
  }
}
