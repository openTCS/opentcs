/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.peripherals.dispatching;

import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.drivers.peripherals.PeripheralJobCallback;

/**
 * Guice configuration for the default peripheral job dispatcher.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DefaultPeripheralJobDispatcherModule
    extends KernelInjectionModule {

  @Override
  protected void configure() {
    configureDispatcherDependencies();
    bindPeripheralJobDispatcher(DefaultPeripheralJobDispatcher.class);
  }

  private void configureDispatcherDependencies() {
    bind(DefaultPeripheralJobDispatcherConfiguration.class)
        .toInstance(getConfigBindingProvider().get(DefaultPeripheralJobDispatcherConfiguration.PREFIX,
                                                   DefaultPeripheralJobDispatcherConfiguration.class));

    bind(PeripheralJobCallback.class).to(DefaultPeripheralJobDispatcher.class);
    bind(PeripheralReleaseStrategy.class).to(DefaultPeripheralReleaseStrategy.class);
    bind(JobSelectionStrategy.class).to(DefaultJobSelectionStrategy.class);
  }
}
