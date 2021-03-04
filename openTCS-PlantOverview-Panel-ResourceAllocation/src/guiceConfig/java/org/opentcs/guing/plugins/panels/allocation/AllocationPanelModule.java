/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.allocation;

import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the resource allocation panel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AllocationPanelModule
    extends PlantOverviewInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AllocationPanelModule.class);

  @Override
  protected void configure() {
    ResourceAllocationPanelConfiguration configuration
        = getConfigBindingProvider().get(ResourceAllocationPanelConfiguration.PREFIX,
                                         ResourceAllocationPanelConfiguration.class);

    if (!configuration.enable()) {
      LOG.info("Resource allocation panel disabled by configuration.");
      return;
    }

    pluggablePanelFactoryBinder().addBinding().to(ResourceAllocationPanelFactory.class);
  }
}
