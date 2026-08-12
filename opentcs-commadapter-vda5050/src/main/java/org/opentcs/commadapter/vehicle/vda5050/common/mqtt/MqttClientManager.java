// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common.mqtt;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.common.mqtt.ConnectionCallback.CONNECT_CONTEXT;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages an MQTT client and its connection to a server/broker.
 */
public class MqttClientManager {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(MqttClientManager.class);
  /**
   * A map from topics to corresponding subscriptions managed by this class.
   */
  private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();
  /**
   * Configuration for the comm adapter.
   */
  private final MqttConfiguration configuration;
  /**
   * The MQTT client.
   */
  private MqttAsyncClient client;
  /**
   * The connect options to use.
   */
  private MqttConnectOptions connectOptions;
  /**
   * A callback for connecting/disconnecting to a server.
   */
  private ConnectionCallback connectionCallback;
  /**
   * A callback for communication events.
   */
  private CommunicationCallback communicationCallback;
  /**
   * Whether a connection is established or not.
   */
  private volatile boolean connected;
  /**
   * The executor to run tasks on.
   */
  private final ScheduledExecutorService kernelExecutor;

  /**
   * Creates a new instance.
   *
   * @param configuration The VDA 5050 adapter configuration.
   * @param kernelExecutor The executor to run tasks on.
   * @throws IllegalStateException If there was a problem initializing the MQTT client.
   */
  @SuppressWarnings("this-escape")
  @Inject
  public MqttClientManager(
      MqttConfiguration configuration,
      @KernelExecutor
      ScheduledExecutorService kernelExecutor
  )
      throws IllegalStateException {
    this.configuration = requireNonNull(configuration, "configuration");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    initialize();
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

    communicationCallback.registerConnectionEventListener(listener);
    connectionCallback.registerConnectionEventListener(listener);
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

    communicationCallback.unregisterConnectionEventListener(listener);
    connectionCallback.unregisterConnectionEventListener(listener);
  }

  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  public boolean isConnected() {
    return connected;
  }

  public MqttAsyncClient getClient() {
    return client;
  }

  /**
   * Publish a message to a topic.
   *
   * @param topic The topic to publish to.
   * @param qos The quality of service to publish with.
   * @param message The message to publish.
   * @param retained Whether or not the message is retained.
   */
  public void publish(
      @Nonnull
      String topic,
      @Nonnull
      QualityOfService qos,
      @Nonnull
      String message,
      boolean retained
  ) {
    requireNonNull(topic, "topic");
    requireNonNull(qos, "qos");
    requireNonNull(message, "message");

    MqttMessage mqttMessage = new MqttMessage(message.getBytes());
    mqttMessage.setQos(qos.getQosValue());
    mqttMessage.setRetained(retained);
    try {
      client.publish(topic, mqttMessage);
    }
    catch (MqttException ex) {
      LOG.error("Failed to publish message to topic '{}'", topic, ex);
    }
  }

  /**
   * Subscribes to the given topic with the given {@link QualityOfService} and registers the given
   * {@link ConnectionEventListener} to be notified when a message is received on the given topic.
   * <p>
   * In case the underlying MQTT client is already subscribed to the given topic, the given
   * {@link ConnectionEventListener} is merely registered to be notified when a message is received
   * on the given topic.
   *
   * @param topic The topic to subscribe to.
   * @param qos The {@link QualityOfService} to subscribe with.
   * @param listener The {@link ConnectionEventListener} that is interested in messages received on
   * the given topic.
   */
  public void subscribe(
      @Nonnull
      String topic,
      @Nonnull
      QualityOfService qos,
      @Nonnull
      ConnectionEventListener listener
  ) {
    requireNonNull(topic, "topic");
    requireNonNull(qos, "qos");
    requireNonNull(listener, "listener");

    Subscription subscription = subscriptions.get(topic);
    if (subscription == null) {
      LOG.debug("Subscribing to topic '{}'...", topic);
      subscription = new Subscription(topic, qos, new CopyOnWriteArrayList<>());
      subscription.getSubscribers().add(listener);
      subscriptions.put(topic, subscription);
      subscribe(topic, qos);
    }
    else {
      LOG.debug("Adding listener to already subscribed topic '{}'...", topic);
      subscription.getSubscribers().add(listener);
    }
  }

  /**
   * Unregisters the given {@link ConnectionEventListener} to no longer be notified when a message
   * is received on the given topic.
   * <p>
   * In case no more {@link ConnectionEventListener}s are registered after the given one has been
   * unregistered, the underlying MQTT client unsubscribes from the given topic.
   *
   * @param topic The topic to unsubscribe from.
   * @param listener The {@link ConnectionEventListener} that is no longer interested in messages
   * received on the given topic.
   */
  public void unsubscribe(
      @Nonnull
      String topic,
      @Nonnull
      ConnectionEventListener listener
  ) {
    requireNonNull(topic, "topic");
    requireNonNull(listener, "listener");

    LOG.debug("Unsubscribing from topic '{}'...", topic);
    if (subscriptions.containsKey(topic)) {
      subscriptions.get(topic).getSubscribers().remove(listener);

      if (subscriptions.get(topic).getSubscribers().isEmpty()) {
        subscriptions.remove(topic);
        unsubscribe(topic);
      }
    }
  }

  /**
   * Sets the last will for underlying MQTT client.
   *
   * @param topic The topic to publish the last will message on.
   * @param message The last will message.
   * @param qos The {@link QualityOfService} for the last will message.
   * @param retained If the last will message is retained.
   */
  public void setLastWill(
      @Nonnull
      String topic,
      @Nonnull
      String message,
      @Nonnull
      QualityOfService qos,
      boolean retained
  ) {
    requireNonNull(topic, "topic");
    requireNonNull(message, "message");
    requireNonNull(qos, "qos");

    if (client.isConnected()) {
      return;
    }

    connectOptions.setWill(topic, message.getBytes(), qos.getQosValue(), retained);
  }

  /**
   * This method is invoked when a message arrives from the server.
   *
   * @param topic The topic the message arrived on.
   * @param message The message.
   */
  public void messageArrived(
      @Nonnull
      String topic,
      @Nonnull
      MqttMessage message
  ) {
    requireNonNull(topic, "topic");
    requireNonNull(message, "message");

    if (subscriptions.containsKey(topic)) {
      IncomingMessage msg = new IncomingMessage(topic, message.toString());
      subscriptions.get(topic).getSubscribers().forEach(
          listener -> listener.onIncomingMessage(msg)
      );
    }
  }

  /**
   * Retry the last connection attempt.
   * If already connected, this does nothing.
   */
  public void retryConnect() {
    if (isConnected()) {
      return;
    }

    LOG.info("Scheduling broker reconnect in {} ms...", configuration.reconnectInterval());
    kernelExecutor.schedule(
        () -> connect(),
        configuration.reconnectInterval(),
        TimeUnit.MILLISECONDS
    );
  }

  /**
   * This method is invoked when the client successfully connected to the broker.
   */
  public void onConnect() {
    LOG.info("Connected.");
    subscriptions.values().forEach(
        subscription -> subscribe(subscription.getTopic(), subscription.getQos())
    );
  }

  private void initialize()
      throws IllegalStateException {
    try {
      communicationCallback = new CommunicationCallback(this);
      connectionCallback = new ConnectionCallback(this);
      connectOptions = new MqttConnectOptions();
      connectOptions.setCleanSession(true);
      connectOptions.setUserName(configuration.username());
      connectOptions.setPassword(configuration.password().toCharArray());
      // In case of a value of zero for the keep-alive, use that, which disables the keep-alive
      // mechanism. Otherwise, ensure we set a value of at least one second.
      connectOptions.setKeepAliveInterval(
          configuration.keepAliveInterval() <= 0
              ? 0
              : Math.max(configuration.keepAliveInterval(), 1000) / 1000
      );

      client = new MqttAsyncClient(
          String.format(
              "%s://%s:%s",
              configuration.connectionEncrypted() ? "ssl" : "tcp",
              configuration.brokerHost(),
              configuration.brokerPort()
          ),
          configuration.clientId(),
          new MemoryPersistence()
      );
      client.setCallback(communicationCallback);

      DisconnectedBufferOptions dbo = new DisconnectedBufferOptions();
      dbo.setBufferEnabled(true);
      client.setBufferOpts(dbo);

      connect();
    }
    catch (MqttException ex) {
      // This should never happen.
      throw new IllegalStateException("Failed to initialize the MQTT client instance", ex);
    }
  }

  private void connect() {
    if (isConnected()) {
      LOG.debug("Already connected, doing nothing.");
      return;
    }

    LOG.info(
        "Initiating connection attempt to {}:{} with client ID '{}'...",
        configuration.brokerHost(),
        configuration.brokerPort(),
        configuration.clientId()
    );
    try {
      client.connect(connectOptions, CONNECT_CONTEXT, connectionCallback);
    }
    catch (MqttException ex) {
      LOG.error("Error while connecting to the server.", ex);
    }
  }

  private void subscribe(String topic, QualityOfService qos) {
    try {
      LOG.info("Subscribing to topic '{}' with QoS {}...", topic, qos.getQosValue());
      client.subscribe(topic, qos.getQosValue());
    }
    catch (MqttException ex) {
      LOG.error("Failed to subscribe to topic '{}'.", topic, ex);
    }
  }

  private void unsubscribe(String topic) {
    try {
      client.unsubscribe(topic);
    }
    catch (MqttException ex) {
      LOG.error("Failed to unsubscribe from topic '{}'.", topic, ex);
    }
  }

  private class Subscription {

    /**
     * The topic to subscribe to.
     */
    private final String topic;
    /**
     * The quality of service to subscribe with.
     */
    private final QualityOfService qos;
    /**
     * List of listeners subscribed to this topic.
     */
    private final List<ConnectionEventListener> subscribers;

    Subscription(
        @Nonnull
        String topic,
        @Nonnull
        QualityOfService qos,
        @Nonnull
        List<ConnectionEventListener> subscribers
    ) {
      this.topic = requireNonNull(topic, "topic");
      this.qos = requireNonNull(qos, "qos");
      this.subscribers = requireNonNull(subscribers, "subscribers");
    }

    @Nonnull
    public String getTopic() {
      return topic;
    }

    @Nonnull
    public QualityOfService getQos() {
      return qos;
    }

    @Nonnull
    public List<ConnectionEventListener> getSubscribers() {
      return subscribers;
    }
  }
}
