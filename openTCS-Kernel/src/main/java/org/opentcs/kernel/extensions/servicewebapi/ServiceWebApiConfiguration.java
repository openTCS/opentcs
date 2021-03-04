/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi;

import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;

/**
 * Configuration entries for the service web API.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@ConfigurationPrefix(ServiceWebApiConfiguration.PREFIX)
public interface ServiceWebApiConfiguration {

  String PREFIX = "servicewebapi";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to enable the interface.",
      orderKey = "0")
  boolean enable();

  @ConfigurationEntry(
      type = "IP address",
      description = "Address to which to bind the HTTP server, e.g. 0.0.0.0 or 127.0.0.1.",
      orderKey = "1")
  String bindAddress();

  @ConfigurationEntry(
      type = "Integer",
      description = "Port to which to bind the HTTP server.",
      orderKey = "2")
  int bindPort();

  @ConfigurationEntry(
      type = "String",
      description = "Key allowing access to the API.",
      orderKey = "3")
  String accessKey();

  @ConfigurationEntry(
      type = "Integer",
      description = "Maximum number of status events to be kept.",
      orderKey = "4")
  int statusEventsCapacity();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to use SSL to encrypt connections.",
      orderKey = "5")
  boolean useSsl();
}
