/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.statistics;

import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;

/**
 * Configuration entries for the statistics collector.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@ConfigurationPrefix(StatisticsCollectorConfiguration.PREFIX)
public interface StatisticsCollectorConfiguration {

  String PREFIX = "statisticscollector";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to enable the statistics collector.",
      orderKey = "0")
  boolean enable();
}
