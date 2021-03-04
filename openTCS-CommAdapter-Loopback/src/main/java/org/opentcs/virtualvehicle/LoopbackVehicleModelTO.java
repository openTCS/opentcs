/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;

/**
 * A serializable representation of a {@link LoopbackVehicleModel}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LoopbackVehicleModelTO
    extends VehicleProcessModelTO {

  /**
   * Whether this communication adapter is in single step mode or not (i.e. in automatic mode).
   */
  private boolean singleStepModeEnabled;
  /**
   * Indicates which operation is a loading operation.
   */
  private String loadOperation;
  /**
   * Indicates which operation is an unloading operation.
   */
  private String unloadOperation;
  /**
   * The time needed for executing operations.
   */
  private int operatingTime;
  /**
   * The maximum acceleration.
   */
  private int maxAcceleration;
  /**
   * The maximum deceleration.
   */
  private int maxDeceleration;
  /**
   * The maximum forward velocity.
   */
  private int maxFwdVelocity;
  /**
   * The maximum reverse velocity.
   */
  private int maxRevVelocity;
  /**
   * Whether the vehicle is paused or not.
   */
  private boolean vehiclePaused;

  public boolean isSingleStepModeEnabled() {
    return singleStepModeEnabled;
  }

  public LoopbackVehicleModelTO setSingleStepModeEnabled(boolean singleStepModeEnabled) {
    this.singleStepModeEnabled = singleStepModeEnabled;
    return this;
  }

  public String getLoadOperation() {
    return loadOperation;
  }

  public LoopbackVehicleModelTO setLoadOperation(String loadOperation) {
    this.loadOperation = loadOperation;
    return this;
  }

  public String getUnloadOperation() {
    return unloadOperation;
  }

  public LoopbackVehicleModelTO setUnloadOperation(String unloadOperation) {
    this.unloadOperation = unloadOperation;
    return this;
  }

  public int getOperatingTime() {
    return operatingTime;
  }

  public LoopbackVehicleModelTO setOperatingTime(int operatingTime) {
    this.operatingTime = operatingTime;
    return this;
  }

  public int getMaxAcceleration() {
    return maxAcceleration;
  }

  public LoopbackVehicleModelTO setMaxAcceleration(int maxAcceleration) {
    this.maxAcceleration = maxAcceleration;
    return this;
  }

  public int getMaxDeceleration() {
    return maxDeceleration;
  }

  public LoopbackVehicleModelTO setMaxDeceleration(int maxDeceleration) {
    this.maxDeceleration = maxDeceleration;
    return this;
  }

  public int getMaxFwdVelocity() {
    return maxFwdVelocity;
  }

  public LoopbackVehicleModelTO setMaxFwdVelocity(int maxFwdVelocity) {
    this.maxFwdVelocity = maxFwdVelocity;
    return this;
  }

  public int getMaxRevVelocity() {
    return maxRevVelocity;
  }

  public LoopbackVehicleModelTO setMaxRevVelocity(int maxRevVelocity) {
    this.maxRevVelocity = maxRevVelocity;
    return this;
  }

  public boolean isVehiclePaused() {
    return vehiclePaused;
  }

  public LoopbackVehicleModelTO setVehiclePaused(boolean vehiclePaused) {
    this.vehiclePaused = vehiclePaused;
    return this;
  }
}
