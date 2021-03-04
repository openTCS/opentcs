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
      type = "String",
      description = {"The type of encryption used for RMI.",
                     "'NONE': No encryption.",
                     "'SSL_UNTRUSTED': SSL is used, but there is no way to verify the remote peer's "
                         + "identity. (Default)",
                     "'SSL': SSL is used. (Generation of a keystore-truststore-pair is required.)"},
      orderKey = "0_address_3")
  ConnectionEncryption connectionEncryption();

  @ConfigurationEntry(
      type = "Long",
      description = "The interval for cleaning out inactive clients (in ms).",
      orderKey = "1_sweeping")
  long clientSweepInterval();

  enum ConnectionEncryption {
    NONE,
    SSL_UNTRUSTED,
    SSL;
  }
}
