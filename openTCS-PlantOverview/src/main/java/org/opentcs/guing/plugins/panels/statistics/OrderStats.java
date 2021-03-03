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
 * Statistics data for a transport order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class OrderStats
    extends Stats {

  /**
   * When the order was activated.
   */
  private long activationTime;
  /**
   * When the order was assigned to a vehicle.
   */
  private long assignmentTime;
  /**
   * When the order reached a final state.
   */
  private long finishedTime;
  /**
   * Whether the order was finished successfully or not.
   */
  private boolean finishedSuccessfully;
  /**
   * Whether processing of the order crossed its deadline or not.
   */
  private boolean crossedDeadline;

  /**
   * Creates a new instance.
   *
   * @param name The name of the order.
   * @param totalRuntime The total runtime recorded.
   */
  public OrderStats(final String name, final long totalRuntime) {
    super(name, totalRuntime);
  }

  /**
   * Returns the point of time at which the order was activated.
   *
   * @return The point of time at which the order was activated.
   */
  public long getActivationTime() {
    return activationTime;
  }

  /**
   * Returns the point of time at which the order was assigned to a vehicle.
   *
   * @return The point of time at which the order was assigned to a vehicle.
   */
  public long getAssignmentTime() {
    return assignmentTime;
  }

  /**
   * Returns the point of time at which the order reached a final state.
   *
   * @return The point of time at which the order reached a final state.
   */
  public long getFinishedTime() {
    return finishedTime;
  }

  /**
   * Returns whether or not the order was finished successfully.
   *
   * @return Whether or not the order was finished successfully.
   */
  public boolean isFinishedSuccessfully() {
    return finishedSuccessfully;
  }

  /**
   * Returns whether or not processing of the order crossed its deadline.
   *
   * @return Whether or not processing of the order crossed its deadline.
   */
  public boolean hasCrossedDeadline() {
    return crossedDeadline;
  }

  /**
   * Sets the order's activation time to the given timestamp.
   *
   * @param timestamp The order's activation time.
   */
  public void activate(long timestamp) {
    assert timestamp > 0;
    activationTime = timestamp;
  }

  /**
   * Sets the order's assignment time to the given timestamp.
   *
   * @param timestamp The order's assignment time.
   */
  public void assign(long timestamp) {
    // If the activation time wasn't set, set it now.
    if (activationTime <= 0) {
      activationTime = timestamp;
    }
    assignmentTime = timestamp;
  }

  /**
   * Sets the order's finished time to the given timestamp.
   *
   * @param timestamp The order's finished time.
   * @param success Whether or not the order was finished successfully.
   */
  public void finish(long timestamp, boolean success) {
    // If the activation time wasn't set, set it now.
    if (activationTime <= 0) {
      activationTime = timestamp;
    }
    // If the assignment time wasn't set, set it now.
    if (assignmentTime <= 0) {
      assignmentTime = timestamp;
    }
    finishedTime = timestamp;
    finishedSuccessfully = success;
  }

  /**
   * Indicates that the order's deadline was crossed.
   */
  public void crossDeadline() {
    crossedDeadline = true;
  }
}
