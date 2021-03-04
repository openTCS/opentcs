/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the load generator panel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LoadGeneratorPanelModule
    extends PlantOverviewInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LoadGeneratorPanelModule.class);

  @Override
  protected void configure() {
    ContinuousLoadPanelConfiguration configuration
        = getConfigBindingProvider().get(ContinuousLoadPanelConfiguration.PREFIX,
                                         ContinuousLoadPanelConfiguration.class);

    if (!configuration.enable()) {
      LOG.info("Continuous load panel disabled by configuration.");
      return;
    }

    // tag::documentation_createPluginPanelModule[]
    pluggablePanelFactoryBinder().addBinding().to(ContinuousLoadPanelFactory.class);
    // end::documentation_createPluginPanelModule[]
  }
}
