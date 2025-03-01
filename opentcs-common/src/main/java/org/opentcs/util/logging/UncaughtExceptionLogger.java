// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An UncaughtExceptionHandler that logs everything not caught and then exits.
 */
public class UncaughtExceptionLogger
    implements
      Thread.UncaughtExceptionHandler {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(UncaughtExceptionLogger.class);
  /**
   * A flag indicating whether to exit on uncaught exceptions or not.
   */
  private final boolean doExit;

  /**
   * Creates a new UncaughtExceptionLogger.
   *
   * @param exitOnException A flag indicating whether to exit on uncaught
   * exceptions or not.
   */
  public UncaughtExceptionLogger(boolean exitOnException) {
    super();
    doExit = exitOnException;
  }

  @Override
  public void uncaughtException(Thread t, Throwable e) {
    // Log the exception, and then get out of here.
    LOG.error("Unhandled exception", e);
    if (doExit) {
      System.exit(1);
    }
  }
}
