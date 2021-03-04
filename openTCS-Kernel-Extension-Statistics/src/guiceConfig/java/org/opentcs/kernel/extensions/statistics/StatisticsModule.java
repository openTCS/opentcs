/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.statistics;

import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the statistics extension.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StatisticsModule
    extends KernelInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StatisticsModule.class);

  @Override
  protected void configure() {
    StatisticsCollectorConfiguration configuration
        = getConfigBindingProvider().get(StatisticsCollectorConfiguration.PREFIX,
                                         StatisticsCollectorConfiguration.class);
    if (!configuration.enable()) {
      LOG.info("Statistics disabled by configuration.");
      return;
    }

    bind(StatisticsCollectorConfiguration.class)
        .toInstance(configuration);
    extensionsBinderOperating().addBinding()
        .to(StatisticsCollector.class);
  }
}
