/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.adminwebapi;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Configuration entries for the administration web API.
 */
@ConfigurationPrefix(AdminWebApiConfiguration.PREFIX)
public interface AdminWebApiConfiguration {

  /**
   * The prefix for all configuration entries here.
   */
  String PREFIX = "adminwebapi";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to enable the admin interface.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "0"
  )
  boolean enable();

  @ConfigurationEntry(
      type = "IP address",
      description = "Address to which to bind the HTTP server, e.g. 0.0.0.0. (Default: 127.0.0.1.)",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "1"
  )
  String bindAddress();

  @ConfigurationEntry(
      type = "Integer",
      description = "Port to which to bind the HTTP server.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "2"
  )
  int bindPort();

}
