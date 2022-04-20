/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.peripheral.loopback;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loopback adapter-specific Gucie configuration for the Kernel.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LoopbackPeripheralKernelModule
    extends KernelInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LoopbackPeripheralKernelModule.class);

  @Override
  protected void configure() {
    VirtualPeripheralConfiguration configuration
        = getConfigBindingProvider().get(VirtualPeripheralConfiguration.PREFIX,
                                         VirtualPeripheralConfiguration.class);

    if (!configuration.enable()) {
      LOG.info("Peripheral loopback driver disabled by configuration.");
      return;
    }

    bind(VirtualPeripheralConfiguration.class)
        .toInstance(configuration);

    install(new FactoryModuleBuilder().build(LoopbackPeripheralAdapterComponentsFactory.class));

    // tag::documentation_createCommAdapterModule[]
    peripheralCommAdaptersBinder().addBinding().to(LoopbackPeripheralCommAdapterFactory.class);
    // end::documentation_createCommAdapterModule[]
  }
}
