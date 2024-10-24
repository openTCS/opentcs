// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.application;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import org.opentcs.common.ClientConnectionMode;
import org.opentcs.common.KernelClientApplication;
import org.opentcs.common.PortalManager;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.operationsdesk.util.OperationsDeskConfiguration;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The application for kernel connections.
 */
public class OperationsDeskApplication
    implements
      KernelClientApplication,
      EventHandler {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory
      .getLogger(OperationsDeskApplication.class);
  /**
   * The portal manager.
   */
  private final PortalManager portalManager;
  /**
   * The application's event bus.
   */
  private final EventBus eventBus;
  /**
   * The application's configuration.
   */
  private final OperationsDeskConfiguration configuration;
  /**
   * Whether this application is online or not.
   */
  private ConnectionState connectionState = ConnectionState.OFFLINE;
  /**
   * Whether this application is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param portalManager The service portal manager.
   * @param eventBus The application's event bus.
   * @param configuration The application's configuration.
   */
  @Inject
  @SuppressWarnings("this-escape")
  public OperationsDeskApplication(
      @Nonnull
      PortalManager portalManager,
      @ApplicationEventBus
      EventBus eventBus,
      OperationsDeskConfiguration configuration
  ) {
    this.portalManager = requireNonNull(portalManager, "portalManager");
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    eventBus.subscribe(this);

    online(configuration.useBookmarksWhenConnecting());

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }
    eventBus.unsubscribe(this);
    // If we want to terminate but are still online, go offline first
    offline();

    initialized = false;
  }

  @Override
  public void onEvent(Object event) {
  }

  @Override
  public void online(boolean autoConnect) {

    if (isOnline() || isConnecting()) {
      return;
    }

    connectionState = ConnectionState.CONNECTING;
    if (portalManager.connect(toConnectionMode(autoConnect))) {
      LOG.info("Switching application state to online...");
      connectionState = ConnectionState.ONLINE;
      eventBus.onEvent(ClientConnectionMode.ONLINE);
    }
    else {
      connectionState = ConnectionState.OFFLINE;
    }
  }

  @Override
  public void offline() {
    if (!isOnline() && !isConnecting()) {
      return;
    }

    portalManager.disconnect();

    LOG.info("Switching application state to offline...");
    connectionState = ConnectionState.OFFLINE;
    eventBus.onEvent(ClientConnectionMode.OFFLINE);
  }

  @Override
  public boolean isOnline() {
    return connectionState == ConnectionState.ONLINE;
  }

  /**
   * Returns <code>true</code> if, and only if the operations desk is trying to establish a
   * connection to the kernel.
   *
   * @return <code>true</code> if, and only if the operations desk is trying to establish a
   * connection to the kernel
   */
  public boolean isConnecting() {
    return connectionState == ConnectionState.CONNECTING;
  }

  private PortalManager.ConnectionMode toConnectionMode(boolean autoConnect) {
    return autoConnect ? PortalManager.ConnectionMode.AUTO : PortalManager.ConnectionMode.MANUAL;
  }

  /**
   * An enum to display the different states of the operations desk application connection to the
   * kernel.
   */
  private enum ConnectionState {
    /**
     * The operations desk is not connected to the kernel and is not trying to.
     */
    OFFLINE,
    /**
     * The operations desk is currently trying to connect to the kernel.
     */
    CONNECTING,
    /**
     * The operations desk is connected to the kernel.
     */
    ONLINE
  }
}
