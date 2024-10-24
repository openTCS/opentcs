// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the {@link DefaultRouter}.
 */
@ConfigurationPrefix(DefaultRouterConfiguration.PREFIX)
public interface DefaultRouterConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "defaultrouter";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to compute a route even if the vehicle is already at the destination.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY
  )
  boolean routeToCurrentPosition();

}
