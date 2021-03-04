/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.common;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the {@link ScheduledThreadPoolExecutor} by logging exceptions thrown by scheduled tasks.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LoggingScheduledThreadPoolExecutor
    extends ScheduledThreadPoolExecutor {

  /**
   * This class's logger.
   */
  private static final Logger LOG
      = LoggerFactory.getLogger(LoggingScheduledThreadPoolExecutor.class);

  /**
   * Creates a new instance.
   *
   * @param corePoolSize The number of threads to keep in the pool.
   * @param threadFactory The factory to use when the executor creates a new thread.
   * @throws IllegalArgumentException If {@code corePoolSize < 0}
   * @throws NullPointerException If {@code threadFactory} is null
   */
  public LoggingScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
    super(corePoolSize, threadFactory);
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    super.afterExecute(r, t);
    if (t == null && r instanceof Future<?>) {
      try {
        Future<?> future = (Future<?>) r;
        if (future.isDone()) {
          future.get();
        }
        else if (isPeriodic(future)) {
          // Periodic futures will never be done
          return;
        }
        else {
          LOG.debug("Future was not done: {}", future);
        }
      }
      catch (ExecutionException ee) {
        LOG.warn("Unhandled exception in executed task", ee.getCause());
      }
      catch (CancellationException ce) {
        LOG.debug("Task was cancelled", ce);
      }
      catch (InterruptedException ie) {
        LOG.debug("Interrupted during Future.get()", ie);
        // Ignore/Reset
        Thread.currentThread().interrupt();
      }
    }
    if (t != null) {
      LOG.error("Abrupt termination", t);
    }
  }

  private boolean isPeriodic(Future<?> future) {
    if (future instanceof RunnableScheduledFuture<?>) {
      RunnableScheduledFuture<?> runnableFuture = (RunnableScheduledFuture<?>) future;
      if (runnableFuture.isPeriodic()) {
        return true;
      }
    }
    return false;
  }
}
