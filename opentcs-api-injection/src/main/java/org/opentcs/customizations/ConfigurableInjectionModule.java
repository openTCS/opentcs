// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.customizations;

import com.google.inject.AbstractModule;

/**
 * A base class for Guice modules adding or customizing bindings for the kernel application and the
 * plant overview application.
 */
public abstract class ConfigurableInjectionModule
    extends
      AbstractModule {

  /**
   * A provider for configuration bindings.
   */
  private org.opentcs.configuration.ConfigurationBindingProvider configBindingProvider;

  /**
   * Returns the configuration bindung provider.
   *
   * @return The configuration binding provider.
   */
  public org.opentcs.configuration.ConfigurationBindingProvider getConfigBindingProvider() {
    return configBindingProvider;
  }

  /**
   * Sets the configuration binding provider.
   *
   * @param configBindingProvider The new configuration binding provider.
   */
  public void setConfigBindingProvider(
      org.opentcs.configuration.ConfigurationBindingProvider configBindingProvider
  ) {
    this.configBindingProvider = configBindingProvider;
  }
}
