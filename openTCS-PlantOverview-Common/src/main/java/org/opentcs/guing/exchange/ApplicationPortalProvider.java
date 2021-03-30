/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange;

import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.common.PortalManager;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.guing.util.PlantOverviewApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a {@link KernelClientPortal} for clients in the kernel control center application.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ApplicationPortalProvider
    implements SharedKernelServicePortalProvider {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ApplicationPortalProvider.class);
  /**
   * The registered clients.
   */
  private final Set<Object> clients = new HashSet<>();
  /**
   * The portal manager taking care of the portal connection.
   */
  private final PortalManager portalManager;
  /**
   * The application's configuration.
   */
  private final PlantOverviewApplicationConfiguration configuration;

  /**
   * Creates a new instance.
   *
   * @param portalManager The portal manager taking care of the portal connection.
   * @param configuration The application's configuration.
   */
  @Inject
  public ApplicationPortalProvider(PortalManager portalManager,
                                   PlantOverviewApplicationConfiguration configuration) {
    this.portalManager = requireNonNull(portalManager, "ortalManager");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public SharedKernelServicePortal register()
      throws ServiceUnavailableException {
    Object token = new Object();
    if (!portalShared()) {
      LOG.debug("Initiating portal connection for new client...");
      portalManager.connect(toConnectionMode(configuration.useBookmarksWhenConnecting()));
    }
    clients.add(token);

    if (!portalShared()) {
      unregister(token);
      throw new ServiceUnavailableException("Could not connect to portal");
    }
    return new ApplicationPortal(portalManager.getPortal(),
                                 this,
                                 token);
  }

  public synchronized boolean unregister(Object client) {
    requireNonNull(client, "client");

    if (clients.remove(client)) {
      if (clients.isEmpty()) {
        LOG.debug("Last client left. Terminating portal connection...");
        portalManager.disconnect();
      }
      return true;
    }
    return false;
  }

  @Override
  public synchronized boolean portalShared() {
    return portalManager.isConnected();
  }

  @Override
  public synchronized String getPortalDescription() {
    return portalShared()
        ? portalManager.getHost() + ":" + portalManager.getPort() : "-";
  }

  private PortalManager.ConnectionMode toConnectionMode(boolean autoConnect) {
    return autoConnect ? PortalManager.ConnectionMode.AUTO : PortalManager.ConnectionMode.MANUAL;
  }
}
