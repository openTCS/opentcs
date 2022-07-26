/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import org.opentcs.configuration.ConfigurationBindingProvider;
import org.opentcs.configuration.cfg4j.Cfg4jConfigurationBindingProvider;
import org.opentcs.customizations.ConfigurableInjectionModule;
import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;
import org.opentcs.guing.common.util.CompatibilityChecker;
import org.opentcs.operationsdesk.application.PlantOverviewStarter;
import org.opentcs.util.Environment;
import org.opentcs.util.logging.UncaughtExceptionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The plant overview process's default entry point.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RunOperationsDesk {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RunOperationsDesk.class);

  /**
   * Prevents external instantiation.
   */
  private RunOperationsDesk() {
  }

  /**
   * The plant overview client's main entry point.
   *
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    System.setSecurityManager(new SecurityManager());
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger(false));

    Environment.logSystemInfo();
    ensureVersionCompatibility();

    Injector injector = Guice.createInjector(customConfigurationModule());
    injector.getInstance(PlantOverviewStarter.class).startPlantOverview();
  }

  private static void ensureVersionCompatibility() {
    String version = System.getProperty("java.version");
    if (!CompatibilityChecker.versionCompatibleWithDockingFrames(version)) {
      LOG.error("Version incompatible with Docking Frames: '{}'", version);
      CompatibilityChecker.showVersionIncompatibleWithDockingFramesMessage();
      System.exit(1);
    }
  }

  /**
   * Builds and returns a Guice module containing the custom configuration for the plant overview
   * application, including additions and overrides by the user.
   *
   * @return The custom configuration module.
   */
  private static Module customConfigurationModule() {
    ConfigurationBindingProvider bindingProvider = configurationBindingProvider();
    ConfigurableInjectionModule plantOverviewInjectionModule
        = new DefaultPlantOverviewInjectionModule();
    plantOverviewInjectionModule.setConfigBindingProvider(bindingProvider);
    return Modules.override(plantOverviewInjectionModule)
        .with(findRegisteredModules(bindingProvider));
  }

  /**
   * Finds and returns all Guice modules registered via ServiceLoader.
   *
   * @return The registered/found modules.
   */
  private static List<PlantOverviewInjectionModule> findRegisteredModules(
      ConfigurationBindingProvider bindingProvider) {
    List<PlantOverviewInjectionModule> registeredModules = new LinkedList<>();
    for (PlantOverviewInjectionModule module
             : ServiceLoader.load(PlantOverviewInjectionModule.class)) {
      LOG.info("Integrating injection module {}", module.getClass().getName());
      module.setConfigBindingProvider(bindingProvider);
      registeredModules.add(module);
    }
    return registeredModules;
  }

  private static ConfigurationBindingProvider configurationBindingProvider() {
    return new Cfg4jConfigurationBindingProvider(
        Paths.get(System.getProperty("opentcs.base", "."),
                  "config",
                  "opentcs-operationsdesk-defaults-baseline.properties")
            .toAbsolutePath(),
        Paths.get(System.getProperty("opentcs.base", "."),
                  "config",
                  "opentcs-operationsdesk-defaults-custom.properties")
            .toAbsolutePath(),
        Paths.get(System.getProperty("opentcs.home", "."),
                  "config",
                  "opentcs-operationsdesk.properties")
            .toAbsolutePath()
    );
  }
}
