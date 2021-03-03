/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.statistics;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.opentcs.access.ApplicationHome;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.algorithms.KernelExtension;
import org.opentcs.data.TCSObjectEventFilter;

/**
 * Collects data from kernel events and logs interesting events to a file that
 * can later be processed for statistical purposes.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StatisticsCollector
    implements KernelExtension {

  /**
   * This class's logger.
   */
  private static final Logger log =
      Logger.getLogger(StatisticsEventLogger.class.getName());
  /**
   * The kernel we're working with.
   */
  private final Kernel kernel;
  /**
   * The directory to log event data to.
   */
  private final File logDir;
  /**
   * The file we're writing collected data to.
   */
  private final File logFile;
  /**
   * Indicates whether this instance is currently enabled or not.
   */
  private boolean enabled;
  /**
   * An event listener with the kernel for collecting statistics events.
   */
  private StatisticsEventListener statisticsListener;
  /**
   * An event logger for persisting the event data collected.
   */
  private StatisticsEventLogger statisticsLogger;
  /**
   * A separate thread for logging event data without blocking.
   */
  private Thread loggerThread;

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel we're working with.
   * @param homeDir The kernel's home directory.
   */
  @Inject
  public StatisticsCollector(LocalKernel kernel,
                             @ApplicationHome File homeDir) {
    this.kernel = Objects.requireNonNull(kernel, "kernel is null");
    Objects.requireNonNull(homeDir, "homeDir is null");
    this.logDir = new File(homeDir, "log/statistics");
    if (!logDir.isDirectory() && !logDir.mkdirs()) {
      throw new IllegalArgumentException(logDir.getPath()
          + " is not an existing directory and could not be created, either.");
    }
    Format format = new SimpleDateFormat("yyyyMMdd-HHmmss");
    logFile = new File(logDir,
                       "openTCS-statistics-" + format.format(new Date()) + ".txt");
  }

  @Override
  public boolean isPluggedIn() {
    log.finer("method entry");
    return enabled;
  }

  @Override
  public void plugIn() {
    log.finer("method entry");
    // Only react if the state really changes.
    if (!enabled) {
      try {
        // Create and start queue processor for kernel events.
        log.info("Enabling logging to " + logFile.getAbsolutePath() + "...");
        if (logFile.exists()) {
          // XXX Rename to file name with timestamp?!
          logFile.renameTo(new File(logDir, "openTCS-statistics-txt.old"));
        }
        statisticsLogger = new StatisticsEventLogger(logFile);
        loggerThread = new Thread(statisticsLogger, "statisticsLogger");
        loggerThread.start();
        // Create event listener and register it with the kernel.
        statisticsListener = new StatisticsEventListener(statisticsLogger);
        kernel.addEventListener(statisticsListener,
                                TCSObjectEventFilter.acceptingInstance);
        // Remember we're plugged in.
        enabled = true;
        log.fine("Statistics collector enabled");
      }
      catch (IOException exc) {
        log.log(Level.WARNING, "Exception creating event logger", exc);
        statisticsLogger = null;
      }
    }
  }

  @Override
  public void plugOut() {
    log.finer("method entry");
    // Only react if the state really changes.
    if (enabled) {
      log.fine("Terminating statistics collector...");
      // Unregister event listener, terminate event processor.
      kernel.removeEventListener(statisticsListener);
      statisticsLogger.terminate();
      try {
        loggerThread.join();
        log.fine("Statistics logger thread has terminated.");
      }
      catch (InterruptedException exc) {
        throw new RuntimeException(
            "Interrupted while waiting for statistics logger to die.");
      }
      finally {
        statisticsListener = null;
        statisticsLogger = null;
        enabled = false;
      }
    }
  }
}
