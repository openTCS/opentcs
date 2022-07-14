/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.util;

import java.util.List;
import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;
import org.opentcs.util.gui.dialog.ConnectionParamSet;

/**
 * Provides methods to configure the PlantOverview application.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(PlantOverviewApplicationConfiguration.PREFIX)
public interface PlantOverviewApplicationConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "plantoverviewapp";

  @ConfigurationEntry(
      type = "Comma-separated list of <description>\\|<hostname>\\|<port>",
      description = "Kernel connection bookmarks to be used.",
      orderKey = "2_connection_0")
  List<ConnectionParamSet> connectionBookmarks();

  @ConfigurationEntry(
      type = "Boolean",
      description = {
        "Whether to use the configured bookmarks when connecting to the kernel.",
        "If 'true', the first connection bookmark will be used for the connection attempt.",
        "If 'false', a dialog will be shown to enter connection parameters."},
      orderKey = "2_connection_1")
  boolean useBookmarksWhenConnecting();
}
