/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi;

import javax.inject.Singleton;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the service web API extension.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ServiceWebApiModule
    extends KernelInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ServiceWebApiModule.class);

  @Override
  protected void configure() {
    ServiceWebApiConfiguration configuration
        = getConfigBindingProvider().get(ServiceWebApiConfiguration.PREFIX,
                                         ServiceWebApiConfiguration.class);

    if (!configuration.enable()) {
      LOG.info("Service web API disabled by configuration.");
      return;
    }

    bind(ServiceWebApiConfiguration.class)
        .toInstance(configuration);

    extensionsBinderOperating().addBinding()
        .to(ServiceWebApi.class)
        .in(Singleton.class);
  }
}
