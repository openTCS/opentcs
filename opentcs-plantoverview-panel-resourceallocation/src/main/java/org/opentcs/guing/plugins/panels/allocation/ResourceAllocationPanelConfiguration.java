// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.plugins.panels.allocation;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the continuous load panel.
 */
@ConfigurationPrefix(ResourceAllocationPanelConfiguration.PREFIX)
public interface ResourceAllocationPanelConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "resourceallocationpanel";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to enable to register/enable the resource allocation panel.",
      orderKey = "0_enable"
  )
  boolean enable();
}
