// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.adminwebapi;

import jakarta.inject.Singleton;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the admin web API extension.
 */
public class AdminWebApiModule
    extends
      KernelInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AdminWebApiModule.class);

  /**
   * Creates a new instance.
   */
  public AdminWebApiModule() {
  }

  @Override
  protected void configure() {
    AdminWebApiConfiguration configuration
        = getConfigBindingProvider().get(
            AdminWebApiConfiguration.PREFIX,
            AdminWebApiConfiguration.class
        );

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
