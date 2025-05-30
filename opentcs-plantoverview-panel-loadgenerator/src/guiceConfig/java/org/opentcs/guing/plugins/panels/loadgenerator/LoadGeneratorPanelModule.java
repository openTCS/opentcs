// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.plugins.panels.loadgenerator;

import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the load generator panel.
 */
public class LoadGeneratorPanelModule
    extends
      PlantOverviewInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LoadGeneratorPanelModule.class);

  /**
   * Creates a new instance.
   */
  public LoadGeneratorPanelModule() {
  }

  @Override
  protected void configure() {
    ContinuousLoadPanelConfiguration configuration
        = getConfigBindingProvider().get(
            ContinuousLoadPanelConfiguration.PREFIX,
            ContinuousLoadPanelConfiguration.class
        );

    if (!configuration.enable()) {
      LOG.info("Continuous load panel disabled by configuration.");
      return;
    }

    // tag::documentation_createPluginPanelModule[]
    pluggablePanelFactoryBinder().addBinding().to(ContinuousLoadPanelFactory.class);
    // end::documentation_createPluginPanelModule[]
  }
}
