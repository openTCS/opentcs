/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import org.opentcs.customizations.ConfigurableInjectionModule;
import org.opentcs.customizations.ConfigurationBindingProvider;
import org.opentcs.customizations.controlcenter.ControlCenterInjectionModule;
import org.opentcs.kernelcontrolcenter.configuration.DefaultConfigurationBindingProvider;
import org.opentcs.util.Environment;
import org.opentcs.util.logging.UncaughtExceptionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The kernel control center process's default entry point.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class RunKernelControlCenter {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RunKernelControlCenter.class);

  /**
   * Prevents external instantiation.
   */
  private RunKernelControlCenter() {
  }

  /**
   * The kernel control center client's main entry point.
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
    injector.getInstance(KernelControlCenterApplication.class).initialize();
  }

  /**
   * Builds and returns a Guice module containing the custom configuration for the kernel control
   * center application, including additions and overrides by the user.
   *
   * @return The custom configuration module.
   */
  private static Module customConfigurationModule() {
    ConfigurationBindingProvider bindingProvider = new DefaultConfigurationBindingProvider();
    ConfigurableInjectionModule kernelControlCenterInjectionModule
        = new DefaultKernelControlCenterInjectionModule();
    kernelControlCenterInjectionModule.setConfigBindingProvider(bindingProvider);
    return Modules.override(kernelControlCenterInjectionModule)
        .with(findRegisteredModules(bindingProvider));
  }

  /**
   * Finds and returns all Guice modules registered via ServiceLoader.
   *
   * @return The registered/found modules.
   */
  private static List<ControlCenterInjectionModule> findRegisteredModules(
      ConfigurationBindingProvider bindingProvider) {
    List<ControlCenterInjectionModule> registeredModules = new LinkedList<>();
    for (ControlCenterInjectionModule module
             : ServiceLoader.load(ControlCenterInjectionModule.class)) {
      LOG.info("Integrating injection module {}", module.getClass().getName());
      module.setConfigBindingProvider(bindingProvider);
      registeredModules.add(module);
    }
    return registeredModules;
  }
}
