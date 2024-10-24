// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.plugins.panels.allocation;

import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the resource allocation panel.
 */
public class AllocationPanelModule
    extends
      PlantOverviewInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AllocationPanelModule.class);

  /**
   * Creates a new instance.
   */
  public AllocationPanelModule() {
  }

  @Override
  protected void configure() {
    ResourceAllocationPanelConfiguration configuration
        = getConfigBindingProvider().get(
            ResourceAllocationPanelConfiguration.PREFIX,
            ResourceAllocationPanelConfiguration.class
        );

    if (!configuration.enable()) {
      LOG.info("Resource allocation panel disabled by configuration.");
      return;
    }

    pluggablePanelFactoryBinder().addBinding().to(ResourceAllocationPanelFactory.class);
  }
}
