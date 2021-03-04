/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the continuous load panel.
 *
 * @author Martin Grzenia (Fraunhofer IML)
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
      orderKey = "0_enable")
  boolean enable();
}
