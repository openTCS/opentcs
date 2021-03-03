/*
 * openTCS copyright information:
 * Copyright (c) 2009 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An UncaughtExceptionHandler that logs everything not caught and then exits.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class UncaughtExceptionLogger
    implements Thread.UncaughtExceptionHandler {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(UncaughtExceptionLogger.class.getName());
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
    log.log(Level.SEVERE, "Unhandled exception", e);
    if (doExit) {
      System.exit(1);
    }
  }
}
