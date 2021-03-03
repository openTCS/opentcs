/*
 * openTCS copyright information:
 * Copyright (c) 2015 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.logging.Logger;
import org.opentcs.util.Environment;
import org.opentcs.util.logging.UncaughtExceptionLogger;

/**
 * The kernel process's default entry point.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RunKernel {

  /**
   * This class's Logger.
   */
  private static final Logger log = Logger.getLogger(RunKernel.class.getName());

  /**
   * Initializes the system and starts the openTCS kernel including modules.
   *
   * @param args The command line arguments.
   */
  public static void main(String[] args) {
    System.setSecurityManager(new SecurityManager());
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger(true));

    Environment.logSystemInfo();

    log.fine("Setting up openTCS kernel " + Environment.getVersionString() + "...");
    Injector injector = Guice.createInjector(new KernelInjectionModule());
    injector.getInstance(KernelStarter.class).startKernel();
  }
}
