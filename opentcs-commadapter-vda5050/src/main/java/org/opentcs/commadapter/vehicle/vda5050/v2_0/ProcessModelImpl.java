// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.connection.Connection;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.connection.ConnectionState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.instantactions.InstantActions;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.BatteryState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.EStop;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.OperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.SafetyState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.visualization.Visualization;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleProcessModel;

/**
 * A custom model for the {@link CommAdapterImpl} which holds additional information
 * about the connected vehicle.
 */
public class ProcessModelImpl
    extends
      VehicleProcessModel {

  /**
   * The current/most recent state reported by the vehicle.
   */
  private State currentState;
  /**
   * The previous state reported by the vehicle.
   */
  private State previousState;
  /**
   * The last order sent to the vehicle.
   */
  private Order lastOrderSent;
  /**
   * The last instant actions sent to the vehicle.
   */
  private InstantActions lastInstantActionsSent;
  /**
   * Indicates whether the vehicle has not been heard of recently.
   */
  private boolean vehicleIdle = true;
  /**
   * Whether the comm adapter is connected to the MQTT broker.
   */
  private boolean brokerConnected;
  /**
   * The prefix used for MQTT topic names.
   */
  private String topicPrefix = "";
  /**
   * The current/most recent reported connection message.
   */
  private Connection currentConnection;
  /**
   * The current/most recent reported visualization message.
   */
  private Visualization currentVisualization;

  /**
   * Creates a new instance.
   *
   * @param attachedVehicle The attached vehicle
   */
  public ProcessModelImpl(Vehicle attachedVehicle) {
    super(attachedVehicle);
    previousState = new State(
        "",
        0L,
        "",
        0L,
        new ArrayList<>(),
        new ArrayList<>(),
        false,
        new ArrayList<>(),
        new BatteryState(100.0, false),
        OperatingMode.AUTOMATIC,
        new ArrayList<>(),
        new SafetyState(EStop.NONE, true)
    );
    previousState.setTimestamp(Instant.now());
    currentState = new State(
        "",
        0L,
        "",
        0L,
        new ArrayList<>(),
        new ArrayList<>(),
        false,
        new ArrayList<>(),
        new BatteryState(100.0, false),
        OperatingMode.AUTOMATIC,
        new ArrayList<>(),
        new SafetyState(EStop.NONE, true)
    );
    currentState.setTimestamp(Instant.now());
    currentConnection = new Connection(
        0L,
        Instant.now(),
        "",
        "",
        "",
        ConnectionState.OFFLINE
    );
    currentVisualization = new Visualization(
        0L,
        Instant.now(),
        "",
        "",
        ""
    );
  }

  /**
   * Returns the current/most recent state reported by the vehicle.
   *
   * @return The current/most recent state reported by the vehicle
   */
  @Nonnull
  public State getCurrentState() {
    return currentState;
  }

  /**
   * Sets the current/most recent state reported by the vehicle.
   *
   * @param currentState The current/most recent state reported by the vehicle.
   */
  public void setCurrentState(
      @Nonnull
      State currentState
  ) {
    State oldValue = this.currentState;
    this.currentState = requireNonNull(currentState, "currentState");

    getPropertyChangeSupport().firePropertyChange(
        Attribute.CURRENT_STATE.name(),
        oldValue,
        currentState
    );
  }

  /**
   * Returns the previous state reported by the vehicle.
   *
   * @return The previous state reported by the vehicle
   */
  @Nonnull
  public State getPreviousState() {
    return previousState;
  }

  /**
   * Sets the previous state reported by the vehicle.
   *
   * @param previousState The previous state reported by the vehicle
   */
  public void setPreviousState(
      @Nonnull
      State previousState
  ) {
    State oldValue = this.previousState;
    this.previousState = requireNonNull(previousState, "previousState");

    getPropertyChangeSupport().firePropertyChange(
        Attribute.PREVIOUS_STATE.name(),
        oldValue,
        previousState
    );
  }

  /**
   * Returns the last order sent to the vehicle.
   *
   * @return The last order sent to the vehicle, or <code>null</code>, if no order has been
   * sent to the vehicle, yet.
   */
  @Nullable
  public synchronized Order getLastOrderSent() {
    return lastOrderSent;
  }

  /**
   * Sets the last order sent to the vehicle.
   *
   * @param lastOrderSent The last order sent to the vehicle
   */
  public synchronized void setLastOrderSent(
      @Nullable
      Order lastOrderSent
  ) {
    Order oldValue = this.lastOrderSent;
    this.lastOrderSent = lastOrderSent;

    getPropertyChangeSupport().firePropertyChange(
        Attribute.LAST_ORDER.name(),
        oldValue,
        lastOrderSent
    );
  }

  /**
   * Returns the last instant action sent to the vehicle.
   *
   * @return The last instant action sent to the vehicle, or <code>null</code>, if no instant
   * action has been sent to the vehicle, yet.
   */
  @Nullable
  public synchronized InstantActions getLastInstantActionsSent() {
    return lastInstantActionsSent;
  }

  /**
   * Sets the last instant actions sent to the vehicle.
   *
   * @param lastInstantActionsSent The last instant actions sent to the vehicle
   */
  public synchronized void setLastInstantActionsSent(
      @Nullable
      InstantActions lastInstantActionsSent
  ) {
    InstantActions oldValue = this.lastInstantActionsSent;
    this.lastInstantActionsSent = lastInstantActionsSent;

    getPropertyChangeSupport().firePropertyChange(
        Attribute.LAST_INSTANT_ACTIONS.name(),
        oldValue,
        lastInstantActionsSent
    );
  }

  /**
   * Returns whether the vehicle has not been heard of recently.
   *
   * @return Whether the vehicle has not been heard of recently.
   */
  public boolean isVehicleIdle() {
    return vehicleIdle;
  }

  /**
   * Sets whether the vehicle has not been heard of recently.
   *
   * @param idle Whether the vehicle has not been heard of recently.
   */
  public void setVehicleIdle(boolean idle) {
    boolean oldValue = this.vehicleIdle;
    this.vehicleIdle = idle;

    getPropertyChangeSupport().firePropertyChange(
        Attribute.VEHICLE_IDLE.name(),
        oldValue,
        idle
    );
  }

  /**
   * Returns whether the broker is connected.
   *
   * @return Whether the broker is connected
   */
  public boolean isBrokerConnected() {
    return brokerConnected;
  }

  /**
   * Sets whether the broker is connected or not.
   *
   * @param brokerConnected Whether the broker is connected or not
   */
  public void setBrokerConnected(boolean brokerConnected) {
    boolean oldValue = this.brokerConnected;
    this.brokerConnected = brokerConnected;

    getPropertyChangeSupport().firePropertyChange(
        Attribute.BROKER_CONNECTED.name(),
        oldValue,
        brokerConnected
    );
  }

  /**
   * Returns the prefix used for MQTT topic names.
   *
   * @return The prefix used for MQTT topic names.
   */
  @Nonnull
  public String getTopicPrefix() {
    return topicPrefix;
  }

  /**
   * Sets the prefix used for MQTT topic names.
   *
   * @param topicPrefix The prefix used for MQTT topic names.
   */
  public void setTopicPrefix(
      @Nonnull
      String topicPrefix
  ) {
    String oldValue = this.topicPrefix;
    this.topicPrefix = requireNonNull(topicPrefix, "topicPrefix");

    getPropertyChangeSupport().firePropertyChange(
        Attribute.TOPIC_PREFIX.name(),
        oldValue,
        topicPrefix
    );
  }

  /**
   * Return the current connection message.
   *
   * @return the current connection message.
   */
  public Connection getCurrentConnection() {
    return currentConnection;
  }

  /**
   * Sets the current connection message.
   *
   * @param connection the current connection message.
   */
  public void setCurrentConnection(Connection connection) {
    Connection oldValue = currentConnection;
    currentConnection = connection;

    getPropertyChangeSupport().firePropertyChange(
        Attribute.CONNECTION_MESSAGE.name(),
        oldValue,
        connection
    );
  }

  /**
   * Return the current visualization message.
   *
   * @return the current visualization message.
   */
  public Visualization getCurrentVisualization() {
    return currentVisualization;
  }

  /**
   * Sets the current visualization message.
   *
   * @param visualization the current visualization message.
   */
  public void setCurrentVisualization(Visualization visualization) {
    Visualization oldValue = currentVisualization;
    currentVisualization = visualization;

    getPropertyChangeSupport().firePropertyChange(
        Attribute.VISUALIZATION_MESSAGE.name(),
        oldValue,
        visualization
    );
  }

  /**
   * Model attributes specific to this implementation.
   */
  public enum Attribute {
    /**
     * The last state message received.
     */
    CURRENT_STATE,
    /**
     * The previous state message received.
     */
    PREVIOUS_STATE,
    /**
     * The last order.
     */
    LAST_ORDER,
    /**
     * The last instant actions.
     */
    LAST_INSTANT_ACTIONS,
    /**
     * The vehicle's idle flag.
     */
    VEHICLE_IDLE,
    /**
     * Whether the comm adapter is connected to the MQTT broker.
     */
    BROKER_CONNECTED,
    /**
     * The prefix used for MQTT topic names.
     */
    TOPIC_PREFIX,
    /**
     * The last connection message received.
     */
    CONNECTION_MESSAGE,
    /**
     * The last visualization message received.
     */
    VISUALIZATION_MESSAGE;
  }
}
