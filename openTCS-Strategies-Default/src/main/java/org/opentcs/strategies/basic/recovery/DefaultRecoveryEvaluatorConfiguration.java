/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.recovery;

import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the {@link DefaultRecoveryEvaluator}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(DefaultRecoveryEvaluatorConfiguration.PREFIX)
public interface DefaultRecoveryEvaluatorConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "defaultrecoveryevaluator";

  @ConfigurationEntry(
      type = "Double",
      description = "The minimum NES value interpreted as a positive recovery status.")
  double threshold();
}
