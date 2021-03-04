/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import org.opentcs.configuration.ConfigurationBindingProvider;
import org.opentcs.configuration.cfg4j.Cfg4jConfigurationBindingProvider;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherModule;
import org.opentcs.strategies.basic.recovery.DefaultRecoveryEvaluatorModule;
import org.opentcs.strategies.basic.routing.DefaultRouterModule;
import org.opentcs.strategies.basic.scheduling.DefaultSchedulerModule;
import org.opentcs.util.Environment;
import org.opentcs.util.logging.UncaughtExceptionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The kernel process's default entry point.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RunKernel {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RunKernel.class);

  /**
   * Prevents external instantiation.
   */
  private RunKernel() {
  }

  /**
   * Initializes the system and starts the openTCS kernel including modules.
   *
   * @param args The command line arguments.
   * @throws Exception If there was a problem starting the kernel.
   */
  @SuppressWarnings("deprecation")
  public static void main(String[] args)
      throws Exception {
    System.setSecurityManager(new SecurityManager());
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger(false));
    System.setProperty(org.opentcs.util.configuration.Configuration.PROPKEY_IMPL_CLASS,
                       org.opentcs.util.configuration.XMLConfiguration.class.getName());

    Environment.logSystemInfo();

    LOG.debug("Setting up openTCS kernel {}...", Environment.getBaselineVersion());
    Injector injector = Guice.createInjector(customConfigurationModule());
    injector.getInstance(KernelStarter.class).startKernel();
  }

  /**
   * Builds and returns a Guice module containing the custom configuration for the kernel
   * application, including additions and overrides by the user.
   *
   * @return The custom configuration module.
   */
  private static Module customConfigurationModule() {
    List<KernelInjectionModule> defaultModules
        = Arrays.asList(new DefaultKernelInjectionModule(),
                        new DefaultDispatcherModule(),
                        new DefaultRouterModule(),
                        new DefaultSchedulerModule(),
                        new DefaultRecoveryEvaluatorModule());

    ConfigurationBindingProvider bindingProvider = configurationBindingProvider();
    for (KernelInjectionModule defaultModule : defaultModules) {
      defaultModule.setConfigBindingProvider(bindingProvider);
    }

    return Modules.override(defaultModules)
        .with(findRegisteredModules(bindingProvider));
  }

  /**
   * Finds and returns all Guice modules registered via ServiceLoader.
   *
   * @return The registered/found modules.
   */
  private static List<KernelInjectionModule> findRegisteredModules(
      ConfigurationBindingProvider bindingProvider) {
    List<KernelInjectionModule> registeredModules = new LinkedList<>();
    for (KernelInjectionModule module : ServiceLoader.load(KernelInjectionModule.class)) {
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
                  "opentcs-kernel-defaults-baseline.properties")
            .toAbsolutePath(),
        Paths.get(System.getProperty("opentcs.base", "."),
                  "config",
                  "opentcs-kernel-defaults-custom.properties")
            .toAbsolutePath(),
        Paths.get(System.getProperty("opentcs.home", "."),
                  "config",
                  "opentcs-kernel.properties")
            .toAbsolutePath()
    );
  }
}
