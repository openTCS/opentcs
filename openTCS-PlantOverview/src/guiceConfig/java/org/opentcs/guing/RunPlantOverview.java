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
import java.util.ServiceLoader;
import org.opentcs.customizations.ConfigurableInjectionModule;
import org.opentcs.customizations.ConfigurationBindingProvider;
import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;
import org.opentcs.guing.application.PlantOverviewStarter;
import org.opentcs.guing.configuration.DefaultConfigurationBindingProvider;
import org.opentcs.util.Environment;
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
   * Prevents external instantiation.
   */
  private RunPlantOverview() {
  }

  /**
   * The plant overview client's main entry point.
   *
   * @param args the command line arguments
   */
  @SuppressWarnings("deprecation")
  public static void main(final String[] args) {
    System.setSecurityManager(new SecurityManager());
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger(false));
    System.setProperty(org.opentcs.util.configuration.Configuration.PROPKEY_IMPL_CLASS,
                       org.opentcs.util.configuration.XMLConfiguration.class.getName());

    Environment.logSystemInfo();

    Injector injector = Guice.createInjector(customConfigurationModule());
    injector.getInstance(PlantOverviewStarter.class).startPlantOverview();
  }

  /**
   * Builds and returns a Guice module containing the custom configuration for the plant overview
   * application, including additions and overrides by the user.
   *
   * @return The custom configuration module.
   */
  private static Module customConfigurationModule() {
    ConfigurationBindingProvider bindingProvider = new DefaultConfigurationBindingProvider();
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
}
