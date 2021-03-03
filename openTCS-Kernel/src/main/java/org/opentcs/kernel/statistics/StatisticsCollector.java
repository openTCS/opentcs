/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.statistics;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.data.TCSObjectEventFilter;
import org.opentcs.util.statistics.StatisticsEventLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger LOG = LoggerFactory.getLogger(StatisticsCollector.class);
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
  public boolean isInitialized() {
    LOG.debug("method entry");
    return enabled;
  }

  @Override
  public void initialize() {
    LOG.debug("method entry");
    // Only react if the state really changes.
    if (!enabled) {
      try {
        // Create and start queue processor for kernel events.
        LOG.info("Enabling logging to " + logFile.getAbsolutePath() + "...");
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
                                TCSObjectEventFilter.ACCEPT_ALL);
        // Remember we're plugged in.
        enabled = true;
        LOG.debug("Statistics collector enabled");
      }
      catch (IOException exc) {
        LOG.warn("Exception creating event logger", exc);
        statisticsLogger = null;
      }
    }
  }

  @Override
  public void terminate() {
    LOG.debug("method entry");
    // Only react if the state really changes.
    if (enabled) {
      LOG.debug("Terminating statistics collector...");
      // Unregister event listener, terminate event processor.
      kernel.removeEventListener(statisticsListener);
      statisticsLogger.terminate();
      try {
        loggerThread.join();
        LOG.debug("Statistics logger thread has terminated.");
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
