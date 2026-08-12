// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common.mqtt;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A callback for connecting/disconnecting to a server.
 */
public class ConnectionCallback
    implements
      IMqttActionListener {

  /**
   * The context object for connecting to a server.
   */
  public static final Object CONNECT_CONTEXT = new Object();
  /**
   * The context object for disconnecting from a server.
   */
  public static final Object DISCONNECT_CONTEXT = new Object();
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ConnectionCallback.class);
  /**
   * The client manager.
   */
  private final MqttClientManager clientManager;
  /**
   * List of registered connection event listeners.
   */
  private final List<ConnectionEventListener> connectionEventListeners = new ArrayList<>();

  /**
   * Creates a new instance.
   *
   * @param clientManager The client manager.
   */
  public ConnectionCallback(MqttClientManager clientManager) {
    this.clientManager = requireNonNull(clientManager, "clientManager");
  }

  @Override
  public void onSuccess(IMqttToken asyncActionToken) {

    if (Objects.equals(asyncActionToken.getUserContext(), CONNECT_CONTEXT)) {
      onConnect();
    }
    else if (Objects.equals(asyncActionToken.getUserContext(), DISCONNECT_CONTEXT)) {
      onDisconnect();
    }
    else {
      LOG.warn("Unhandled user context: {}", asyncActionToken.getUserContext());
    }
  }

  @Override
  public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
    if (Objects.equals(asyncActionToken.getUserContext(), CONNECT_CONTEXT)) {
      LOG.warn("Failed to connect to broker", exception);
      connectionEventListeners.forEach(listener -> listener.onFailedConnectionAttempt());
      clientManager.setConnected(false);
      clientManager.retryConnect();
    }
    else if (Objects.equals(asyncActionToken.getUserContext(), DISCONNECT_CONTEXT)) {
      LOG.warn("Failed to disconnect from broker", exception);
    }
  }

  /**
   * Register a connection event listener.
   *
   * @param listener The listener to register.
   */
  public void registerConnectionEventListener(
      @Nonnull
      ConnectionEventListener listener
  ) {
    requireNonNull(listener, "listener");
    connectionEventListeners.add(listener);
  }

  /**
   * Unregister a connection event listener.
   *
   * @param listener The listener to unregister.
   */
  public void unregisterConnectionEventListener(
      @Nonnull
      ConnectionEventListener listener
  ) {
    requireNonNull(listener, "listener");
    connectionEventListeners.remove(listener);
  }

  private void onConnect() {
    LOG.debug("Connected successfully...");
    clientManager.setConnected(true);
    connectionEventListeners.forEach(listener -> listener.onConnect());
    clientManager.onConnect();
  }

  private void onDisconnect() {
    LOG.debug("Disconnected successfully...");
    clientManager.setConnected(false);
    connectionEventListeners.forEach(listener -> listener.onDisconnect());
  }
}
