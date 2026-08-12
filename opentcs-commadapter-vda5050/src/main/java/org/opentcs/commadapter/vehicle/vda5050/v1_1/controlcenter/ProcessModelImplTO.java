// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.controlcenter;

import org.opentcs.commadapter.vehicle.vda5050.v1_1.ProcessModelImpl;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.connection.Connection;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.instantactions.InstantActions;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.State;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.visualization.Visualization;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;

/**
 * A serializable representation of a {@link ProcessModelImpl}.
 * This TO can be sent to other applications responsible for displaying the state of the vehicle,
 * like the control center or the plant overview.
 */
public class ProcessModelImplTO
    extends
      VehicleProcessModelTO {

  private TCSObjectReference<Vehicle> vehicleRef;
  private State currentState;
  private State previousState;
  private Order lastOrderSent;
  private InstantActions lastInstantActionsSent;
  /**
   * The time to wait between periodic state request telegrams.
   */
  private int stateRequestInterval;
  /**
   * How long (in ms) we tolerate not hearing from the vehicle before we consider communication
   * dead.
   */
  private int vehicleIdleTimeout;
  private boolean vehicleIdle;
  /**
   * Whether to close the connection if the vehicle is considered dead.
   */
  private boolean disconnectingOnVehicleIdle;
  /**
   * Whether to reconnect automatically when the vehicle connection times out.
   */
  private boolean reconnectingOnConnectionLoss;
  /**
   * The delay before reconnecting (in ms).
   */
  private int reconnectDelay;
  /**
   * Whether logging should be enabled or not.
   */
  private boolean loggingEnabled;
  private boolean brokerConnected;
  private String topicPrefix;
  private Connection currentConnection;
  private Visualization currentVisualization;

  public ProcessModelImplTO() {
  }

  public TCSObjectReference<Vehicle> getVehicleRef() {
    return vehicleRef;
  }

  public ProcessModelImplTO setVehicleRef(TCSObjectReference<Vehicle> vehicleRef) {
    this.vehicleRef = vehicleRef;
    return this;
  }

  public State getCurrentState() {
    return currentState;
  }

  public ProcessModelImplTO setCurrentState(State currentState) {
    this.currentState = currentState;
    return this;
  }

  public State getPreviousState() {
    return previousState;
  }

  public ProcessModelImplTO setPreviousState(State previousState) {
    this.previousState = previousState;
    return this;
  }

  public Order getLastOrderSent() {
    return lastOrderSent;
  }

  public ProcessModelImplTO setLastOrderSent(Order lastOrderSent) {
    this.lastOrderSent = lastOrderSent;
    return this;
  }

  public InstantActions getLastInstantActionsSent() {
    return lastInstantActionsSent;
  }

  public ProcessModelImplTO setLastInstantActionsSent(InstantActions lastInstantActionsSent) {
    this.lastInstantActionsSent = lastInstantActionsSent;
    return this;
  }

  /**
   * Returns the time to wait between periodic state request telegrams.
   *
   * @return The time to wait between periodic state request telegrams
   */
  public int getStateRequestInterval() {
    return stateRequestInterval;
  }

  /**
   * Sets the time to wait between periodic state request telegrams.
   *
   * @param stateRequestInterval The time to wait between periodic state request telegrams
   * @return This
   */
  public ProcessModelImplTO setStateRequestInterval(int stateRequestInterval) {
    this.stateRequestInterval = stateRequestInterval;
    return this;
  }

  /**
   * Returns how long (in ms) we tolerate not hearing from the vehicle before we consider
   * communication dead.
   *
   * @return How long (in ms) we tolerate not hearing from the vehicle before we consider
   * communication dead
   */
  public int getVehicleIdleTimeout() {
    return vehicleIdleTimeout;
  }

  /**
   * Sets how long (in ms) we tolerate not hearing from the vehicle before we consider communication
   * dead.
   *
   * @param vehicleIdleTimeout How long (in ms) we tolerate not hearing from the vehicle before
   * we consider communication dead
   * @return This
   */
  public ProcessModelImplTO setVehicleIdleTimeout(int vehicleIdleTimeout) {
    this.vehicleIdleTimeout = vehicleIdleTimeout;
    return this;
  }

  public boolean isVehicleIdle() {
    return vehicleIdle;
  }

  public ProcessModelImplTO setVehicleIdle(boolean vehicleIdle) {
    this.vehicleIdle = vehicleIdle;
    return this;
  }

  /**
   * Returns whether to close the connection if the vehicle is considered dead.
   *
   * @return Whether to close the connection if the vehicle is considered dead
   */
  public boolean isDisconnectingOnVehicleIdle() {
    return disconnectingOnVehicleIdle;
  }

  /**
   * Sets whether to close the connection if the vehicle is considered dead.
   *
   * @param disconnectingOnVehicleIdle Whether to close the connection if the vehicle is
   * considered dead
   * @return This
   */
  public ProcessModelImplTO setDisconnectingOnVehicleIdle(boolean disconnectingOnVehicleIdle) {
    this.disconnectingOnVehicleIdle = disconnectingOnVehicleIdle;
    return this;
  }

  /**
   * Returns whether to reconnect automatically when the vehicle connection times out.
   *
   * @return Whether to reconnect automatically when the vehicle connection times out
   */
  public boolean isReconnectingOnConnectionLoss() {
    return reconnectingOnConnectionLoss;
  }

  /**
   * Sets whether to reconnect automatically when the vehicle connection times out.
   *
   * @param reconnectingOnConnectionLoss Whether to reconnect automatically when the vehicle
   * connection times out
   * @return This
   */
  public ProcessModelImplTO setReconnectingOnConnectionLoss(boolean reconnectingOnConnectionLoss) {
    this.reconnectingOnConnectionLoss = reconnectingOnConnectionLoss;
    return this;
  }

  /**
   * Returns the delay before reconnecting (in ms).
   *
   * @return The delay before reconnecting (in ms)
   */
  public int getReconnectDelay() {
    return reconnectDelay;
  }

  /**
   * Sets the delay before reconnecting (in ms).
   *
   * @param reconnectDelay The delay before reconnecting (in ms)
   * @return This
   */
  public ProcessModelImplTO setReconnectDelay(int reconnectDelay) {
    this.reconnectDelay = reconnectDelay;
    return this;
  }

  /**
   * Returns whether logging should be enabled or not.
   *
   * @return Whether logging should be enabled or not
   */
  public boolean isLoggingEnabled() {
    return loggingEnabled;
  }

  /**
   * Sets whether logging should be enabled or not.
   *
   * @param loggingEnabled Whether logging should be enabled or not
   * @return This
   */
  public ProcessModelImplTO setLoggingEnabled(boolean loggingEnabled) {
    this.loggingEnabled = loggingEnabled;
    return this;
  }

  public boolean isBrokerConnected() {
    return brokerConnected;
  }

  public ProcessModelImplTO setBrokerConnected(boolean brokerConnected) {
    this.brokerConnected = brokerConnected;
    return this;
  }

  public String getTopicPrefix() {
    return topicPrefix;
  }

  public ProcessModelImplTO setTopicPrefix(String topicPrefix) {
    this.topicPrefix = topicPrefix;
    return this;
  }

  public Connection getCurrentConnection() {
    return currentConnection;
  }

  public ProcessModelImplTO setCurrentConnection(Connection connection) {
    currentConnection = connection;
    return this;
  }

  public Visualization getCurrentVisualization() {
    return currentVisualization;
  }

  public ProcessModelImplTO setCurrentVisualization(Visualization visualization) {
    currentVisualization = visualization;
    return this;
  }

}
