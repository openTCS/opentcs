/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.controlcenter;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the {@link KernelControlCenter}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(ControlCenterConfiguration.PREFIX)
public interface ControlCenterConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "controlcenter";

  @ConfigurationEntry(
      type = "Boolean",
      description = {"Whether the kernel control center GUI should be enabled on startup.",
                     "(EXPERIMENTAL)"
      })
  boolean enable();

  @ConfigurationEntry(
      type = "String",
      description = {"The application's current language.",
                     "Valid values: 'English', 'German'"
      })
  String language();

  @ConfigurationEntry(
      type = "Integer",
      description = "The maximum number of characters in the logging text area.")
  int loggingAreaCapacity();
}
