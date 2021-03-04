/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.statistics;

/**
 * Statistics data for a vehicle.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class VehicleStats
    extends Stats {

  /**
   * The total time the vehicle was processing orders.
   */
  private long totalTimeProcessing;
  /**
   * When the vehicle last started processing an order.
   */
  private long lastProcessingStart;
  /**
   * The total time the vehicle was charging.
   */
  private long totalTimeCharging;
  /**
   * When the vehicle last started charging.
   */
  private long lastChargingStart;
  /**
   * The total time the vehicle was waiting.
   */
  private long totalTimeWaiting;
  /**
   * When the vehicle last started waiting.
   */
  private long lastWaitingStart;
  /**
   * The total number of orders the vehicle has processed.
   */
  private int totalOrdersProcessed;

  /**
   * Creates a new instance.
   *
   * @param name The name of the order.
   * @param totalRuntime The total runtime recorded.
   */
  public VehicleStats(final String name, final long totalRuntime) {
    super(name, totalRuntime);
  }

  /**
   * Returns the total time the vehicle was processing orders.
   *
   * @return The total time the vehicle was processing orders.
   */
  public long getTotalTimeProcessing() {
    return totalTimeProcessing;
  }

  /**
   * Returns the total time the vehicle was charging.
   *
   * @return The total time the vehicle was charging.
   */
  public long getTotalTimeCharging() {
    return totalTimeCharging;
  }

  /**
   * Returns the total time the vehicle was waiting.
   *
   * @return The total time the vehicle was waiting.
   */
  public long getTotalTimeWaiting() {
    return totalTimeWaiting;
  }

  /**
   * Returns the total number of orders the vehicle has processed.
   *
   * @return The total number of orders the vehicle has processed.
   */
  public int getTotalOrdersProcessed() {
    return totalOrdersProcessed;
  }

  /**
   * Indicates the vehicle started processing an order at the given time.
   *
   * @param timestamp When the vehicle started processing an order.
   */
  public void startProcessingOrder(long timestamp) {
    assert timestamp > 0;
    totalOrdersProcessed++;
    lastProcessingStart = timestamp;
  }

  /**
   * Indicates the vehicle stopped processing an order at the given time.
   *
   * @param timestamp When the vehicle stopped processing an order.
   */
  public void stopProcessingOrder(long timestamp) {
    assert timestamp > 0;
    if (lastProcessingStart != 0) {
      totalTimeProcessing += timestamp - lastProcessingStart;
    }
    lastProcessingStart = 0;
  }

  /**
   * Indicates the vehicle started charging at the given time.
   *
   * @param timestamp When the vehicle started charging.
   */
  public void startCharging(long timestamp) {
    assert timestamp > 0;
    lastChargingStart = timestamp;
  }

  /**
   * Indicates the vehicle stopped charging at the given time.
   *
   * @param timestamp When the vehicle stopped charging.
   */
  public void stopCharging(long timestamp) {
    assert timestamp > 0;
    if (lastChargingStart != 0) {
      totalTimeCharging += timestamp - lastChargingStart;
    }
    lastChargingStart = 0;
  }

  /**
   * Indicates the vehicle started waiting at the given time.
   *
   * @param timestamp When the vehicle started waiting.
   */
  public void startWaiting(long timestamp) {
    assert timestamp > 0;
    lastWaitingStart = timestamp;
  }

  /**
   * Indicates the vehicle stopped waiting at the given time.
   *
   * @param timestamp When the vehicle stopped waiting.
   */
  public void stopWaiting(long timestamp) {
    assert timestamp > 0;
    if (lastWaitingStart != 0) {
      totalTimeWaiting += timestamp - lastWaitingStart;
    }
    lastWaitingStart = 0;
  }
}
