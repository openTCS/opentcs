/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.common;

import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.gui.dialog.ConnectToServerDialog;
import org.opentcs.util.gui.dialog.ConnectionParamSet;
import org.opentcs.util.gui.dialog.NullConnectionParamSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of {@link PortalManager}, providing a single
 * {@link KernelServicePortal}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultPortalManager
    implements PortalManager {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultPortalManager.class);
  /**
   * This class's resource bundle.
   */
  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle("org/opentcs/util/Bundle");
  /**
   * The handler to send events to.
   */
  private final EventHandler eventHandler;
  /**
   * The connection bookmarks to use.
   */
  private final List<ConnectionParamSet> connectionBookmarks;
  /**
   * The service portal instance we are working with.
   */
  private final KernelServicePortal servicePortal;
  /**
   * The last successfully established connection.
   */
  private ConnectionParamSet lastConnection = new NullConnectionParamSet();
  /**
   * The current connection. {@link NullConnectionParamSet}, if no connection is currently
   * established.
   */
  private ConnectionParamSet currentConnection = new NullConnectionParamSet();

  /**
   * Creates a new instance.
   *
   * @param servicePortal The service portal instance we a working with.
   * @param eventHandler The handler to send events to.
   * @param connectionBookmarks The connection bookmarks to use.
   */
  @Inject
  public DefaultPortalManager(KernelServicePortal servicePortal,
                              @ApplicationEventBus EventHandler eventHandler,
                              List<ConnectionParamSet> connectionBookmarks) {
    this.eventHandler = requireNonNull(eventHandler, "eventHandler");
    this.servicePortal = requireNonNull(servicePortal, "servicePortal");
    this.connectionBookmarks = requireNonNull(connectionBookmarks, "connectionBookmarks");
  }

  @Override
  public boolean connect(ConnectionMode mode) {
    if (isConnected()) {
      return true;
    }

    switch (mode) {
      case AUTO:
        if (connectionBookmarks.isEmpty()) {
          LOG.info("Cannot connect automatically. No connection bookmarks available.");
          return false;
        }
        ConnectionParamSet paramSet = connectionBookmarks.get(0);
        return connect(paramSet.getDescription(), paramSet.getHost(), paramSet.getPort());
      case MANUAL:
        return connectWithDialog();
      case RECONNECT:
        if (lastConnection instanceof NullConnectionParamSet) {
          LOG.info("Cannot reconnect. No portal we were previously connected to.");
          return false;
        }
        return connect(lastConnection.getDescription(),
                       lastConnection.getHost(),
                       lastConnection.getPort());
      default:
        LOG.warn("Unhandled connection mode '{}'. Not connecting.", mode.name());
        return false;
    }
  }

  @Override
  public void disconnect() {
    if (!isConnected()) {
      return;
    }

    eventHandler.onEvent(ConnectionState.DISCONNECTING);

    try {
      servicePortal.logout();
    }
    catch (KernelRuntimeException e) {
      LOG.warn("Exception trying to disconnect from remote portal", e);
    }

    lastConnection = currentConnection;
    currentConnection = new NullConnectionParamSet();
    eventHandler.onEvent(ConnectionState.DISCONNECTED);
  }

  @Override
  public boolean isConnected() {
    return !(currentConnection instanceof NullConnectionParamSet);
  }

  @Override
  public KernelServicePortal getPortal() {
    return servicePortal;
  }

  @Override
  public String getDescription() {
    return currentConnection.getDescription();
  }

  @Override
  public String getHost() {
    return currentConnection.getHost();
  }

  @Override
  public int getPort() {
    return currentConnection.getPort();
  }

  /**
   * Tries to establish a connection to the portal.
   *
   * @param host The name of the host running the kernel/portal.
   * @param port The port to connect to.
   * @return {@code true} if, and only if, the connection was established successfully.
   */
  private boolean connect(String description, String host, int port) {
    try {
      eventHandler.onEvent(ConnectionState.CONNECTING);
      servicePortal.login(host, port);
    }
    catch (KernelRuntimeException e) {
      LOG.warn("Failed to connect to remote portal", e);
      eventHandler.onEvent(ConnectionState.DISCONNECTED);
      JOptionPane.showMessageDialog(null,
                                    BUNDLE.getString("DefaultPortalManager.NoConnectionDialog.text"),
                                    BUNDLE.getString("DefaultPortalManager.NoConnectionDialog.text"),
                                    JOptionPane.ERROR_MESSAGE);

      // Retry connection attempt
      return connectWithDialog();
    }

    currentConnection = new ConnectionParamSet(description, host, port);
    eventHandler.onEvent(ConnectionState.CONNECTED);
    return true;
  }

  private boolean connectWithDialog() {
    ConnectToServerDialog dialog = new ConnectToServerDialog(connectionBookmarks);
    dialog.setVisible(true);
    if (dialog.getReturnStatus() == ConnectToServerDialog.RET_OK) {
      return connect(dialog.getDescription(), dialog.getHost(), dialog.getPort());
    }

    return false;
  }
}
