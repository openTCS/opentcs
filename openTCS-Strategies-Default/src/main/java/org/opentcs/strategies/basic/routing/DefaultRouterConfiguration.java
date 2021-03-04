/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the {@link DefaultRouter}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(DefaultRouterConfiguration.PREFIX)
public interface DefaultRouterConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "defaultrouter";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to compute a route even if the vehicle is already at the destination.")
  boolean routeToCurrentPosition();

}
