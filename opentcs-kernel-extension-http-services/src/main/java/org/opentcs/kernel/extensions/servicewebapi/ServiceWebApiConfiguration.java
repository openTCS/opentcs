// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Configuration entries for the service web API.
 */
@ConfigurationPrefix(ServiceWebApiConfiguration.PREFIX)
public interface ServiceWebApiConfiguration {

  /**
   * The prefix for all configuration entries here.
   */
  String PREFIX = "servicewebapi";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to enable the interface.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "0"
  )
  boolean enable();

  @ConfigurationEntry(
      type = "IP address",
      description = "Address to which to bind the HTTP server, e.g. 0.0.0.0 or 127.0.0.1.",
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

  @ConfigurationEntry(
      type = "String",
      description = "Key allowing access to the API.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "3"
  )
  String accessKey();

  @ConfigurationEntry(
      type = "Integer",
      description = "Maximum number of status events to be kept.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "4"
  )
  int statusEventsCapacity();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to use SSL to encrypt connections.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "5"
  )
  boolean useSsl();

  @ConfigurationEntry(
      type = "Integer",
      description = "The maximum size (in MB) that a request body may have for it to be processed.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "6"
  )
  int maxRequestBodySize();
}
