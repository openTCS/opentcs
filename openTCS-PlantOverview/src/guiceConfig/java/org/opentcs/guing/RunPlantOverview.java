/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.opentcs.guing.application.PlantOverviewStarter;
import org.opentcs.guing.util.ApplicationConfiguration;
import org.opentcs.util.Environment;
import org.opentcs.util.logging.UncaughtExceptionLogger;

/**
 * The plant overview process's default entry point.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RunPlantOverview {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(RunPlantOverview.class.getName());

  /**
   * The plant overview client's main entry point.
   *
   * @param args the command line arguments
   */
  public static void main(final String args[]) {
    // Make sure we log everything, especially uncaught exceptions leading to
    // threads terminating.
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger(true));
    Environment.logSystemInfo();

    Injector injector = Guice.createInjector(new PlantOverviewInjectionModule());

    initialize(injector.getInstance(ApplicationConfiguration.class));

    injector.getInstance(PlantOverviewStarter.class).startPlantOverview();
  }

  /**
   * Initializes the application according to the given configuration.
   *
   * @param appConfig The configuration.
   */
  private static void initialize(ApplicationConfiguration appConfig) {
    Locale.setDefault(appConfig.getLocale());
    // Look and feel
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (ClassNotFoundException | InstantiationException |
           IllegalAccessException | UnsupportedLookAndFeelException ex) {
      log.log(Level.WARNING, "Could not set look-and-feel", ex);
    }
    // Show tooltips for 30 seconds (Default: 4 sec)
    ToolTipManager.sharedInstance().setDismissDelay(30 * 1000);
  }
}
