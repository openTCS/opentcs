/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.edgeevaluator;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure {@link EdgeEvaluatorExplicitProperties}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(ExplicitPropertiesConfiguration.PREFIX)
public interface ExplicitPropertiesConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "defaultrouter.edgeevaluator.explicitproperties";

  @ConfigurationEntry(
      type = "String",
      description = {
        "The default value used as the routing cost of an edge if no property is set on the "
        + "corresponding path.",
        "The value should be an integer. "
        + "If it is not, the edge is excluded from routing."})
  String defaultValue();
}
