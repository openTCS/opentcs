/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

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
      type = "Long",
      description = "The interval for cleaning out inactive clients (in ms).")
  long clientSweepInterval();

  @ConfigurationEntry(
      type = "String",
      description = {"The host name/IP address of the RMI registry.",
                     "If 'localhost' and not running already, a RMI registry will be started."})
  String registryHost();

  @ConfigurationEntry(
      type = "Integer",
      description = "The TCP port of the RMI.")
  int registryPort();
}
