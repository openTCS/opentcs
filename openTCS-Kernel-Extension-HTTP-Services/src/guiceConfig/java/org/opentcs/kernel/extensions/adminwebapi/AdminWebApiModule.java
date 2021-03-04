/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.adminwebapi;

import javax.inject.Singleton;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the admin web API extension.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AdminWebApiModule
    extends KernelInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AdminWebApiModule.class);

  @Override
  protected void configure() {
    AdminWebApiConfiguration configuration
        = getConfigBindingProvider().get(AdminWebApiConfiguration.PREFIX,
                                         AdminWebApiConfiguration.class);

    if (!configuration.enable()) {
      LOG.info("Admin web API disabled by configuration.");
      return;
    }

    bind(AdminWebApiConfiguration.class)
        .toInstance(configuration);

    extensionsBinderAllModes().addBinding()
        .to(AdminWebApi.class)
        .in(Singleton.class);
  }
}
