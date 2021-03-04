/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import static org.opentcs.util.Assertions.checkInRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A template for cyclic tasks.
 * Subclasses only need to provide an implementation of
 * <code>runActualTask()</code>, which will be called until the task is
 * terminated by calling <code>terminate()</code>; after each call of
 * <code>runActualTask()</code>, a configurable delay may be inserted.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class CyclicTask
    implements Runnable {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CyclicTask.class);
  /**
   * The time (in ms) that this task sleeps after each execution of
   * <code>runActualTask()</code>.
   */
  private final long sleepTime;
  /**
   * A private object to safely synchronize on.
   */
  private final Object syncObject = new Object();
  /**
   * The thread executing this task.
   */
  private volatile Thread taskThread;
  /**
   * This task's <em>terminated</em> flag.
   */
  private volatile boolean terminated;
  /**
   * Whether this task ignores interrupts occurring while sleeping between
   * executions of the actual task.
   */
  private volatile boolean ignoringInterrupts;

  /**
   * Creates a new CyclicTask.
   *
   * @param tSleep The time to sleep between two executions of the actual
   * task (in milliseconds).
   */
  public CyclicTask(final long tSleep) {
    this.sleepTime = checkInRange(tSleep, 0, Long.MAX_VALUE, "tSleep");
  }

  /**
   * Indicates whether this task has been terminated.
   *
   * @return <code>true</code> if, and only if, this task's
   * <em>terminated</em> flag has been set.
   */
  public boolean isTerminated() {
    return terminated;
  }

  /**
   * Terminates this task before its next execution cycle.
   * This method merely flags the task for termination and returns immediately.
   * If the actual task is currently being executed, its execution will not be
   * interrupted, but it will not be run again after finishing.
   */
  public void terminate() {
    synchronized (syncObject) {
      if (isTerminated()) {
        LOG.warn("Already terminated");
      }

      terminated = true;
      syncObject.notify();
    }
  }

  /**
   * Terminates this task before its next execution cycle and waits for it to
   * finish before returning.
   * (This method waits for termination unless the calling thread is the thread
   * that is executing this task. In that case, this method merely flags this
   * task for termination and returns immediately.)
   */
  public void terminateAndWait() {
    Thread joinThread;

    synchronized (syncObject) {
      if (isTerminated()) {
        LOG.warn("Already terminated");
        return;
      }
      else {
        joinThread = taskThread;
        terminated = true;
        syncObject.notify();
      }
    }
    // Wait for the executing thread to finish - unless the end of
    // execution had already been reached or the executing thread is terminating
    // this task itself. (In the latter case, we would wait forever for the
    // join() to return.)
    if (joinThread != null && joinThread != Thread.currentThread()) {
      try {
        joinThread.join();
      }
      catch (InterruptedException exc) {
        throw new IllegalStateException("Unexpectedly interrupted", exc);
      }
    }
  }

  /**
   * Indicates whether this task is ignoring interrupts while it's sleeping.
   *
   * @return <code>true</code> if, and only if, this task is ignoring interrupts
   * while it's sleeping.
   */
  public boolean isIgnoringInterrupts() {
    return ignoringInterrupts;
  }

  /**
   * Sets/unsets this task's flag for ignoring interrupts during sleep phases.
   *
   * @param ignoreInterrupts If <code>true</code>, this task will ignore
   * interrupts during sleep phases; if <code>false</code>, the
   * <code>run()</code> method will throw an exception when interrupted.
   */
  public void setIgnoringInterrupts(boolean ignoreInterrupts) {
    ignoringInterrupts = ignoreInterrupts;
  }

  @Override
  public void run() {
    LOG.debug("method entry");
    // Save the executing thread for use in terminateAndWait().
    taskThread = Thread.currentThread();
    // Execute the actual task until terminated.
    while (!isTerminated()) {
      LOG.debug("Running actual task...");
      runActualTask();
      // Only sleep if this task is not terminated and the sleep time is not 0.
      if (!isTerminated() && sleepTime > 0) {
        synchronized (syncObject) {
          try {
            syncObject.wait(sleepTime);
          }
          catch (InterruptedException exc) {
            if (!isIgnoringInterrupts() || isTerminated()) {
              LOG.error("Unexpectedly interrupted", exc);
              throw new IllegalStateException("Unexpectedly interrupted", exc);
            }
          }
        }
      }
    }
    // Unset taskThread again - this should prevent problems with join()ing
    // threads (in terminateAndWait()) that execute more than one task
    // subsequently.
    taskThread = null;
    LOG.debug("end of method");
  }

  /**
   * Defines the actual work this task should do in every cycle.
   */
  protected abstract void runActualTask();
}
