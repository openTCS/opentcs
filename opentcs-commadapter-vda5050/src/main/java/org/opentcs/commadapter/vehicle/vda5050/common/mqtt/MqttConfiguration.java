// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common.mqtt;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure common parts of a communication adapter.
 */
@ConfigurationPrefix(MqttConfiguration.PREFIX)
public interface MqttConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "commadapter.vehicle.vda5050.mqtt";

  @ConfigurationEntry(
      type = "String",
      description = "See driver documentation.",
      orderKey = "0_brokerHost"
  )
  String brokerHost();

  @ConfigurationEntry(
      type = "Integer",
      description = "See driver documentation.",
      orderKey = "1_brokerPort"
  )
  int brokerPort();

  @ConfigurationEntry(
      type = "Boolean",
      description = "See driver documentation.",
      orderKey = "2_connectionEncrypted"
  )
  boolean connectionEncrypted();

  @ConfigurationEntry(
      type = "String",
      description = "See driver documentation.",
      orderKey = "3_brokerUsername"
  )
  String username();

  @ConfigurationEntry(
      type = "String",
      description = "See driver documentation.",
      orderKey = "4_brokerPassword"
  )
  String password();

  @ConfigurationEntry(
      type = "String",
      description = "See driver documentation.",
      orderKey = "5_clientId"
  )
  String clientId();

  @ConfigurationEntry(
      type = "Integer",
      description = "See driver documentation.",
      orderKey = "6_keepAliveInterval"
  )
  int keepAliveInterval();

  @ConfigurationEntry(
      type = "Integer",
      description = "See driver documentation.",
      orderKey = "7_reconnectInterval"
  )
  int reconnectInterval();
}
