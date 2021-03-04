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
      }
      catch (CancellationException ce) {
        // Ignore
      }
      catch (ExecutionException ee) {
        LOG.error("Unhandled exception", ee.getCause());
      }
      catch (InterruptedException ie) {
        // Ignore/Reset
        Thread.currentThread().interrupt();
      }
    }
  }
}
