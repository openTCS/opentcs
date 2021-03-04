/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the {@link OrderCleanerTask}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(OrderPoolConfiguration.PREFIX)
public interface OrderPoolConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "orderpool";

  @ConfigurationEntry(
      type = "Long",
      description = "The interval between sweeps (in ms).")
  long sweepInterval();

  @ConfigurationEntry(
      type = "Integer",
      description = "The minimum age of orders to remove in a sweep (in ms).")
  int sweepAge();
}
