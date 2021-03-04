/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.statistics;

import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the statistics panel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StatisticsPanelModule
    extends PlantOverviewInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StatisticsPanelModule.class);

  @Override
  protected void configure() {
    StatisticsPanelConfiguration configuration
        = getConfigBindingProvider().get(StatisticsPanelConfiguration.PREFIX,
                                         StatisticsPanelConfiguration.class);

    if (!configuration.enable()) {
      LOG.info("Statistics panel disabled by configuration.");
      return;
    }

    pluggablePanelFactoryBinder().addBinding().to(StatisticsPanelFactory.class);
  }
}
