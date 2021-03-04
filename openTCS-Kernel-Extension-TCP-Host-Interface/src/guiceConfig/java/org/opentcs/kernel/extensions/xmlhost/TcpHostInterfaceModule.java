/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.xmlhost;

import javax.inject.Singleton;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.kernel.extensions.xmlhost.orders.XMLTelegramOrderReceiver;
import org.opentcs.kernel.extensions.xmlhost.status.StatusMessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the TCP host interface extension.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TcpHostInterfaceModule
    extends KernelInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TcpHostInterfaceModule.class);

  @Override
  protected void configure() {
    XMLHostInterfaceConfiguration configuration
        = getConfigBindingProvider().get(XMLHostInterfaceConfiguration.PREFIX,
                                         XMLHostInterfaceConfiguration.class);

    if (!configuration.enable()) {
      LOG.info("TCP host interface disabled by configuration.");
      return;
    }

    bind(XMLHostInterfaceConfiguration.class)
        .toInstance(configuration);
    // The status channel is available in all modes.
    extensionsBinderAllModes().addBinding()
        .to(StatusMessageDispatcher.class)
        .in(Singleton.class);

    // The order interface is available only in operating mode.
    extensionsBinderOperating().addBinding()
        .to(XMLTelegramOrderReceiver.class);
  }
}
