/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A generic implementation of a queue processor.
 * Elements to be processed are added via
 * {@link QueueProcessor#addToQueue(java.lang.Object)}. Subclasses of this class
 * implement {@link QueueProcessor#processQueueElement(java.lang.Object)}, which
 * will be called for each element in the queue to be processed (in the same
 * order they were added to the queue). Optionally, a subclass may override
 * {@link QueueProcessor#terminated()} to be notified when queue processing has
 * finished/stopped and to be be able to free resources, close files etc..
 *
 * @param <E> The type of queue elements.
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class QueueProcessor<E>
    implements Runnable {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(QueueProcessor.class.getName());
  /**
   * A flag indicating whether this task has been terminated.
   */
  private volatile boolean terminated;
  /**
   * A queue for processable objects.
   */
  private final Queue<E> queue = new LinkedList<>();

  /**
   * Creates a empty QueueProcessor.
   */
  public QueueProcessor() {
    // Do nada.
  }

  @Override
  public final void run() {

    while (!terminated) {
      E element = null;
      synchronized (queue) {
        // Wait until there is a command to be processed or we're terminated.
        while (!terminated && queue.isEmpty()) {
          try {
            queue.wait();
          }
          catch (InterruptedException exc) {
            log.log(Level.WARNING, "Unexpectedly interrupted, ignored", exc);
          }
        }
        if (!terminated) {
          element = queue.poll();
        }
      }
      if (element != null) {
        processQueueElement(element);
      }
    }
    terminated();
    log.fine("Processing task terminated.");
  }

  /**
   * Adds an element to the queue.
   * 
   * @param newElement The element to be added.
   */
  public final void addToQueue(E newElement) {
    Objects.requireNonNull(newElement, "newElement is null");

    synchronized (queue) {
      queue.add(newElement);
      queue.notify();
    }
  }

  /**
   * Removes an element from the queue.
   * 
   * @param rmElement The element to be removed.
   */
  public final void removeFromQueue(E rmElement) {
    Objects.requireNonNull(rmElement, "rmElement is null");

    synchronized (queue) {
      queue.remove(rmElement);
    }
  }

  /**
   * Terminates this task.
   */
  public final void terminate() {
    terminated = true;
    synchronized (queue) {
      queue.notify();
    }
  }
  
  /**
   * Checks whether this task has been terminated.
   *
   * @return <code>true</code> if, and only if, this task has been terminated.
   */
  public final boolean isTerminated() {
    return terminated;
  }
  
  /**
   * Called when the queue processor task has been terminated and queue
   * processing has finished/stopped.
   * The default implementation does not do anything. Subclasses should override
   * this method to be notified when queue processing has finished/stopped.
   */
  protected void terminated() {
    // Do nada.
  }

  /**
   * Processes a queue element.
   * 
   * @param element The element.
   */
  protected abstract void processQueueElement(E element);
}
