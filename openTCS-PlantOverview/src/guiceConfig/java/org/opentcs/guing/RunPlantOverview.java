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
import com.google.inject.Module;
import com.google.inject.util.Modules;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;
import org.opentcs.guing.application.PlantOverviewStarter;
import org.opentcs.guing.util.ApplicationConfiguration;
import org.opentcs.util.Environment;
import org.opentcs.util.configuration.Configuration;
import org.opentcs.util.configuration.XMLConfiguration;
import org.opentcs.util.logging.UncaughtExceptionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The plant overview process's default entry point.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RunPlantOverview {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RunPlantOverview.class);

  /**
   * The plant overview client's main entry point.
   *
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    System.setSecurityManager(new SecurityManager());
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger(false));
    System.setProperty(Configuration.PROPKEY_IMPL_CLASS, XMLConfiguration.class.getName());

    Environment.logSystemInfo();

    Injector injector = Guice.createInjector(customConfigurationModule());

    initialize(injector.getInstance(ApplicationConfiguration.class));

    injector.getInstance(PlantOverviewStarter.class).startPlantOverview();
  }

  /**
   * Builds and returns a Guice module containing the custom configuration for the plant overview
   * application, including additions and overrides by the user.
   *
   * @return The custom configuration module.
   */
  private static Module customConfigurationModule() {
    return Modules.override(new FixedPlantOverviewInjectionModule()).with(findRegisteredModules());
  }

  /**
   * Finds and returns all Guice modules registered via ServiceLoader.
   *
   * @return The registered/found modules.
   */
  private static List<PlantOverviewInjectionModule> findRegisteredModules() {
    List<PlantOverviewInjectionModule> registeredModules = new LinkedList<>();
    for (PlantOverviewInjectionModule module
         : ServiceLoader.load(PlantOverviewInjectionModule.class)) {
      LOG.info("Integrating injection module {}", module.getClass().getName());
      registeredModules.add(module);
    }
    return registeredModules;
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
      LOG.warn("Could not set look-and-feel", ex);
    }
    // Show tooltips for 30 seconds (Default: 4 sec)
    ToolTipManager.sharedInstance().setDismissDelay(30 * 1000);
  }
}
