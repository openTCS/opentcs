/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.customizations;

import com.google.inject.AbstractModule;

/**
 * A base class for Guice modules adding or customizing bindings for the kernel application and the
 * plant overview application.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public abstract class ConfigurableInjectionModule
    extends AbstractModule {

  /**
   * A provider for configuration bindings.
   */
  private ConfigurationBindingProvider configBindingProvider;

  /**
   * Returns the configuration bindung provider.
   * 
   * @return The configuration binding provider.
   */
  public ConfigurationBindingProvider getConfigBindingProvider() {
    return configBindingProvider;
  }

  /**
   * Sets the configuration binding provider.
   * 
   * @param configBindingProvider The new configuration binding provider.
   */
  public void setConfigBindingProvider(ConfigurationBindingProvider configBindingProvider) {
    this.configBindingProvider = configBindingProvider;
  }
}
