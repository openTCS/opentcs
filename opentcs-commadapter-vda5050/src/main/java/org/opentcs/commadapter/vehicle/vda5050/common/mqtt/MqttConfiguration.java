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
      description = "The IP address or host name of the MQTT broker to be used.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "0_brokerHost"
  )
  String brokerHost();

  @ConfigurationEntry(
      type = "Integer",
      description = "The port number of the MQTT broker to be used.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "1_brokerPort"
  )
  int brokerPort();

  @ConfigurationEntry(
      type = "Boolean",
      description = {
          "Whether to use SSL/TLS encryption for the connection to the MQTT broker.",
          "Note that, for an encrypted connection, the server's certificate must be signed by a CA "
              + "certificate trusted by the system the driver is running on."
      },
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "2_connectionEncrypted"
  )
  boolean connectionEncrypted();

  @ConfigurationEntry(
      type = "String",
      description = {
          "The user name to be used for authenticating with the MQTT broker.",
          "May be set to an arbitrary value in case no authentication is required with the broker."
      },
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "3_brokerUsername"
  )
  String username();

  @ConfigurationEntry(
      type = "String",
      description = {
          "The password to be used for authenticating with the MQTT broker.",
          "May be set to an arbitrary value in case no authentication is required with the broker."
      },
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "4_brokerPassword"
  )
  String password();

  @ConfigurationEntry(
      type = "String",
      description = {
          "The client ID to use for the connection to the MQTT broker.",
          "This should be unique among all clients connecting to the broker."
      },
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "5_clientId"
  )
  String clientId();

  @ConfigurationEntry(
      type = "Integer",
      description = {
          "The keep-alive interval (in ms) for the connection to the MQTT broker.",
          "Can be set to zero to disable the keep-alive mechanism.",
          "Will be rounded down to seconds if non-zero."
      },
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "6_keepAliveInterval"
  )
  int keepAliveInterval();

  @ConfigurationEntry(
      type = "Integer",
      description = "The interval (in ms) for trying to reconnect to the MQTT broker.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "7_reconnectInterval"
  )
  int reconnectInterval();
}
