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
      type = "Integer",
      description = "The maximum number of routes that the router will ever compute for a single"
          + " request.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY
  )
  int routeComputationLimit();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to compute a route even if the vehicle is already at the destination.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY
  )
  boolean routeToCurrentPosition();
}
