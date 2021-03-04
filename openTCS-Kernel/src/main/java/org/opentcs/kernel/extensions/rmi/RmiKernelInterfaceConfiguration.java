/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.rmi;

import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the {@link StandardRemoteKernel}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(RmiKernelInterfaceConfiguration.PREFIX)
public interface RmiKernelInterfaceConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "rmikernelinterface";

  @ConfigurationEntry(
      type = "Boolean",
      description = {"Whether to enable the interface."},
      orderKey = "0")
  Boolean enable();

  @ConfigurationEntry(
      type = "String",
      description = {"The host name/IP address of the RMI registry.",
                     "If 'localhost' and not running already, a RMI registry will be started."},
      orderKey = "0_address_0")
  String registryHost();

  @ConfigurationEntry(
      type = "Integer",
      description = "The TCP port of the RMI.",
      orderKey = "0_address_1")
  int registryPort();

  @ConfigurationEntry(
      type = "Integer",
      description = "The TCP port of the remote kernel.",
      orderKey = "0_address_2")
  int remoteKernelPort();

  @ConfigurationEntry(
      type = "Integer",
      description = "The TCP port of the remote kernel service portal.",
      orderKey = "0_address_3")
  int remoteKernelServicePortalPort();

  @ConfigurationEntry(
      type = "Integer",
      description = "The TCP port of the remote plant model service.",
      orderKey = "0_address_4")
  int remotePlantModelServicePort();

  @ConfigurationEntry(
      type = "Integer",
      description = "The TCP port of the remote transport order service.",
      orderKey = "0_address_5")
  int remoteTransportOrderServicePort();

  @ConfigurationEntry(
      type = "Integer",
      description = "The TCP port of the remote vehicle service.",
      orderKey = "0_address_6")
  int remoteVehicleServicePort();

  @ConfigurationEntry(
      type = "Integer",
      description = "The TCP port of the remote notification service.",
      orderKey = "0_address_7")
  int remoteNotificationServicePort();

  @ConfigurationEntry(
      type = "Integer",
      description = "The TCP port of the remote scheduler service.",
      orderKey = "0_address_8")
  int remoteSchedulerServicePort();

  @ConfigurationEntry(
      type = "Integer",
      description = "The TCP port of the remote router service.",
      orderKey = "0_address_9")
  int remoteRouterServicePort();

  @ConfigurationEntry(
      type = "Integer",
      description = "The TCP port of the remote dispatcher service.",
      orderKey = "0_address_10")
  int remoteDispatcherServicePort();

  @ConfigurationEntry(
      type = "Long",
      description = "The interval for cleaning out inactive clients (in ms).",
      orderKey = "2_sweeping")
  long clientSweepInterval();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to use SSL to encrypt connections.",
      orderKey = "0_address_11")
  boolean useSsl();
}
