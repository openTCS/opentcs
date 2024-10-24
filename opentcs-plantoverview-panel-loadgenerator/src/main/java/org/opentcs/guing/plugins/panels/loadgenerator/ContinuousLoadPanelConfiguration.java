// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.plugins.panels.loadgenerator;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the continuous load panel.
 */
@ConfigurationPrefix(ContinuousLoadPanelConfiguration.PREFIX)
public interface ContinuousLoadPanelConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "continuousloadpanel";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to enable to register/enable the continuous load panel.",
      orderKey = "0_enable"
  )
  boolean enable();
}
