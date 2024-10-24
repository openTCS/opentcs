// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures/binds the loopback communication adapters of the openTCS kernel.
 */
public class LoopbackCommAdapterModule
    extends
      KernelInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LoopbackCommAdapterModule.class);

  /**
   * Creates a new instance.
   */
  public LoopbackCommAdapterModule() {
  }

  @Override
  protected void configure() {
    VirtualVehicleConfiguration configuration
        = getConfigBindingProvider().get(
            VirtualVehicleConfiguration.PREFIX,
            VirtualVehicleConfiguration.class
        );

    if (!configuration.enable()) {
      LOG.info("Loopback driver disabled by configuration.");
      return;
    }

    bind(VirtualVehicleConfiguration.class)
        .toInstance(configuration);

    install(new FactoryModuleBuilder().build(LoopbackAdapterComponentsFactory.class));

    // tag::documentation_createCommAdapterModule[]
    vehicleCommAdaptersBinder().addBinding().to(LoopbackCommunicationAdapterFactory.class);
    // end::documentation_createCommAdapterModule[]
  }

}
