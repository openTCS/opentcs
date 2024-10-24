// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.peripherals.dispatching;

import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.drivers.peripherals.PeripheralJobCallback;

/**
 * Guice configuration for the default peripheral job dispatcher.
 */
public class DefaultPeripheralJobDispatcherModule
    extends
      KernelInjectionModule {

  /**
   * Creates a new instance.
   */
  public DefaultPeripheralJobDispatcherModule() {
  }

  @Override
  protected void configure() {
    configureDispatcherDependencies();
    bindPeripheralJobDispatcher(DefaultPeripheralJobDispatcher.class);
  }

  private void configureDispatcherDependencies() {
    bind(DefaultPeripheralJobDispatcherConfiguration.class)
        .toInstance(
            getConfigBindingProvider().get(
                DefaultPeripheralJobDispatcherConfiguration.PREFIX,
                DefaultPeripheralJobDispatcherConfiguration.class
            )
        );

    bind(PeripheralJobCallback.class).to(DefaultPeripheralJobDispatcher.class);
    bind(PeripheralReleaseStrategy.class).to(DefaultPeripheralReleaseStrategy.class);
    bind(JobSelectionStrategy.class).to(DefaultJobSelectionStrategy.class);
  }
}
