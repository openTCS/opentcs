// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common.mqtt;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A callback for publishing messages to/receiving messages for a topic.
 */
class CommunicationCallback
    implements
      MqttCallback {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CommunicationCallback.class);
  /**
   * The client manager.
   */
  private final MqttClientManager clientManager;
  /**
   * List of listeners that handle incoming messages and events concerning
   * an established connection.
   */
  private final List<ConnectionEventListener> connectionEventListeners = new ArrayList<>();

  /**
   * Creates a new instance.
   *
   * @param clientManager The client manager.
   */
  CommunicationCallback(MqttClientManager clientManager) {
    this.clientManager = requireNonNull(clientManager, "clientManager");
  }

  @Override
  public void connectionLost(Throwable cause) {
    LOG.warn("Lost connection to broker: {}", cause.getMessage());
    clientManager.setConnected(false);
    connectionEventListeners.forEach(listener -> listener.onDisconnect());
    clientManager.retryConnect();
  }

  @Override
  public void messageArrived(String topic, MqttMessage message)
      throws Exception {
    clientManager.messageArrived(topic, message);
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    LOG.debug("Delivery complete...");
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
}
