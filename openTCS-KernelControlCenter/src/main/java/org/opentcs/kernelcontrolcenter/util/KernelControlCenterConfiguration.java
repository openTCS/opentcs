/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.util;

import java.util.List;
import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;
import org.opentcs.util.gui.dialog.ConnectionParamSet;

/**
 * Provides methods to configure the KernelControlCenter application.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(KernelControlCenterConfiguration.PREFIX)
public interface KernelControlCenterConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "kernelcontrolcenter";

  @ConfigurationEntry(
      type = "String",
      description = {"The kernel control center application's locale.",
                     "Valid values: 'English', 'German'"},
      orderKey = "0_init_0")
  String language();

  @ConfigurationEntry(
      type = "List of <description>\\|<hostname>\\|<port>",
      description = "The configured connection bookmarks.",
      orderKey = "1_connection_0")
  List<ConnectionParamSet> connectionBookmarks();

  @ConfigurationEntry(
      type = "Boolean",
      description = {"Whether to connect automatically on startup.",
                     "If 'true', the first connection bookmark will be used for the initial "
                     + "connection attempt.",
                     "If 'false', a dialog will be shown to enter connection parameters."},
      orderKey = "1_connection_1")
  boolean connectAutomaticallyOnStartup();

  @ConfigurationEntry(
      type = "Integer",
      description = "The maximum number of characters in the logging text area.",
      orderKey = "9_misc")
  int loggingAreaCapacity();

}
