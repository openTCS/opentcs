/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
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
}
