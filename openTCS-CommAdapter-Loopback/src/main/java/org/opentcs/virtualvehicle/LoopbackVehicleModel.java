/*
 * openTCS copyright information:
 * Copyright (c) 2016 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleProcessModel;

/**
 * An observable model of a virtual vehicle's and its comm adapter's attributes.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LoopbackVehicleModel
    extends VehicleProcessModel
    implements VelocityListener {

  /**
   * Indicates whether this communication adapter is in single step mode or not (i.e. in automatic
   * mode).
   */
  private boolean singleStepModeEnabled;
  /**
   * Additional specifications for operations.
   */
  private final Map<String, OperationSpec> operationSpecs = new HashMap<>();
  /**
   * The time needed for executing operations without explicit operation times.
   */
  private int defaultOperatingTime = 5000;
  /**
   * The velocity controller for calculating the simulated vehicle's velocity and current position.
   */
  private final VelocityController velocityController
      = new VelocityController(-500, 500, -1000, 1000);
  /**
   * Amount of energy that is consumed during Movement per second. [W]
   */
  private double movementPower;
  /**
   * Amount of energy that is consumed during Operation per second. [W]
   */
  private double operationPower;
  /**
   * Amount of energy that is consumed during idle state per second. [W]
   */
  private double idlePower;
  /**
   * Keeps a log of recent velocity values.
   */
  private final VelocityHistory velocityHistory = new VelocityHistory(100, 10);

  public LoopbackVehicleModel(Vehicle attachedVehicle) {
    super(attachedVehicle);
  }

  /**
   * Sets this communication adapter's <em>single step mode</em> flag.
   *
   * @param mode If <code>true</code>, sets this adapter to single step mode,
   * otherwise sets this adapter to flow mode.
   */
  public synchronized void setSingleStepModeEnabled(final boolean mode) {
    boolean oldValue = singleStepModeEnabled;
    singleStepModeEnabled = mode;

    getPropertyChangeSupport().firePropertyChange(Attribute.SINGLE_STEP_MODE.name(),
                                                  oldValue,
                                                  mode);
  }

  /**
   * Returns this communication adapter's <em>single step mode</em> flag.
   *
   * @return <code>true</code> if, and only if, this adapter is currently in
   * single step mode.
   */
  public synchronized boolean isSingleStepModeEnabled() {
    return singleStepModeEnabled;
  }

  /**
   * Returns the additional operation specifications.
   *
   * @return operation specification
   */
  @Nonnull
  public synchronized Map<String, OperationSpec> getOperationSpecs() {
    return operationSpecs;
  }

  /**
   * Sets the operation specifications.
   *
   * @param operationSpecs The new operation specifications.
   */
  public synchronized void setOperationSpecs(@Nonnull Map<String, OperationSpec> operationSpecs) {
    requireNonNull(operationSpecs, "operationSpecs");
    
    Map<String, OperationSpec> oldValue = new HashMap<>(this.operationSpecs);
    this.operationSpecs.clear();
    this.operationSpecs.putAll(operationSpecs);
    
    getPropertyChangeSupport().firePropertyChange(Attribute.OPERATION_SPECS.name(),
                                                  oldValue,
                                                  operationSpecs);
  }

  /**
   * Returns the default operating time.
   *
   * @return The default operating time
   */
  public synchronized int getDefaultOperatingTime() {
    return defaultOperatingTime;
  }

  /**
   * Sets the default operating time.
   *
   * @param defaultOperatingTime The new default operating time
   */
  public synchronized void setDefaultOperatingTime(int defaultOperatingTime) {
    int oldValue = this.defaultOperatingTime;
    this.defaultOperatingTime = defaultOperatingTime;
    
    getPropertyChangeSupport().firePropertyChange(Attribute.DEFAULT_OPERATING_TIME.name(),
                                                  oldValue,
                                                  defaultOperatingTime);
  }

  /**
   * Returns the maximum deceleration.
   *
   * @return The maximum deceleration
   */
  public synchronized int getMaxDeceleration() {
    return velocityController.getMaxDeceleration();
  }

  /**
   * Sets the maximum deceleration.
   *
   * @param maxDeceleration The new maximum deceleration
   */
  public synchronized void setMaxDeceleration(int maxDeceleration) {
    int oldValue = velocityController.getMaxDeceleration();
    velocityController.setMaxDeceleration(maxDeceleration);
    
    getPropertyChangeSupport().firePropertyChange(Attribute.MAX_DECELERATION.name(),
                                                  oldValue,
                                                  maxDeceleration);
  }

  /**
   * Returns the maximum acceleration.
   *
   * @return The maximum acceleration
   */
  public synchronized int getMaxAcceleration() {
    return velocityController.getMaxAcceleration();
  }

  /**
   * Sets the maximum acceleration.
   *
   * @param maxAcceleration The new maximum acceleration
   */
  public synchronized void setMaxAcceleration(int maxAcceleration) {
    int oldValue = velocityController.getMaxAcceleration();
    velocityController.setMaxAcceleration(maxAcceleration);
    
    getPropertyChangeSupport().firePropertyChange(Attribute.MAX_ACCELERATION.name(),
                                                  oldValue,
                                                  maxAcceleration);
  }

  /**
   * Returns the maximum reverse velocity.
   *
   * @return The maximum reverse velocity.
   */
  public synchronized int getMaxRevVelocity() {
    return velocityController.getMaxRevVelocity();
  }

  /**
   * Sets the maximum reverse velocity.
   *
   * @param maxRevVelocity The new maximum reverse velocity
   */
  public synchronized void setMaxRevVelocity(int maxRevVelocity) {
    int oldValue = velocityController.getMaxRevVelocity();
    velocityController.setMaxRevVelocity(maxRevVelocity);
    
    getPropertyChangeSupport().firePropertyChange(Attribute.MAX_REVERSE_VELOCITY.name(),
                                                  oldValue,
                                                  maxRevVelocity);
  }

  /**
   * Returns the maximum forward velocity.
   *
   * @return The maximum forward velocity.
   */
  public synchronized int getMaxFwdVelocity() {
    return velocityController.getMaxFwdVelocity();
  }

  /**
   * Sets the maximum forward velocity.
   *
   * @param maxFwdVelocity The new maximum forward velocity.
   */
  public synchronized void setMaxFwdVelocity(int maxFwdVelocity) {
    int oldValue = velocityController.getMaxFwdVelocity();
    velocityController.setMaxFwdVelocity(maxFwdVelocity);
    
    getPropertyChangeSupport().firePropertyChange(Attribute.MAX_FORWARD_VELOCITY.name(),
                                                  oldValue,
                                                  maxFwdVelocity);
  }

  /**
   * Returns whether the vehicle is paused.
   *
   * @return paused
   */
  public synchronized boolean isVehiclePaused() {
    return velocityController.isVehiclePaused();
  }

  /**
   * Pause the vehicle (i.e. set it's velocity to zero).
   *
   * @param pause True, if vehicle shall be paused. False, otherwise.
   */
  public synchronized void setVehiclePaused(boolean pause) {
    boolean oldValue = velocityController.isVehiclePaused();
    velocityController.setVehiclePaused(pause);
    
    getPropertyChangeSupport().firePropertyChange(Attribute.VEHICLE_PAUSED.name(),
                                                  oldValue,
                                                  pause);
  }

  /**
   * Returns the virtual vehicle's velocity controller.
   *
   * @return The virtual vehicle's velocity controller.
   */
  @Nonnull
  public VelocityController getVelocityController() {
    return velocityController;
  }

  /**
   * Set the energy that is consumed during movement per second.
   *
   * @param power power in W
   */
  public void setMovementPower(double power) {
    double oldValue = movementPower;
    movementPower = power;
    
    getPropertyChangeSupport().firePropertyChange(Attribute.MOVEMENT_POWER.name(),
                                                  oldValue,
                                                  power);
  }

  /**
   * Get the energy that is consumed during movement per second.
   *
   * @return power in W
   */
  public double getMovementPower() {
    return movementPower;
  }

  /**
   * Set the energy that is consumed during operation per second.
   *
   * @param power power in W
   */
  public void setOperationPower(double power) {
    double oldValue = operationPower;
    operationPower = power;
    
    getPropertyChangeSupport().firePropertyChange(Attribute.OPERATION_POWER.name(),
                                                  oldValue,
                                                  power);
  }

  /**
   * Get the energy that is consumed during operation per second.
   *
   * @return power in W
   */
  public double getOperationPower() {
    return operationPower;
  }

  /**
   * Set the energy that is consumed during idle state per second.
   *
   * @param power power in W
   */
  public void setIdlePower(double power) {
    double oldValue = idlePower;
    idlePower = power;
    
    getPropertyChangeSupport().firePropertyChange(Attribute.IDLE_POWER.name(),
                                                  oldValue,
                                                  power);
  }

  /**
   * Get the energy that is consumed during idle state per second.
   *
   * @return power in W
   */
  public double getIdlePower() {
    return idlePower;
  }

  /**
   * Returns a log of recent velocity values of the vehicle.
   *
   * @return A log of recent velocity values.
   */
  @Nonnull
  public VelocityHistory getVelocityHistory() {
    return velocityHistory;
  }

  @Override
  public void addVelocityValue(int velocityValue) {
    // Store the new value in the history...
    velocityHistory.addVelocityValue(velocityValue);
    // ...and let all observers know about it.
    getPropertyChangeSupport().firePropertyChange(Attribute.VELOCITY_HISTORY.name(),
                                                  null,
                                                  velocityHistory);
  }

  /**
   * Notification arguments to indicate some change.
   */
  public static enum Attribute {
    /**
     * Indicates a change of the virtual vehicle's single step mode setting.
     */
    SINGLE_STEP_MODE,
    /**
     * Indicates a change of the virtual vehicle's operation specs.
     */
    OPERATION_SPECS,
    /**
     * Indicates a change of the virtual vehicle's default operating time.
     */
    DEFAULT_OPERATING_TIME,
    /**
     * Indicates a change of the virtual vehicle's maximum acceleration.
     */
    MAX_ACCELERATION,
    /**
     * Indicates a change of the virtual vehicle's maximum deceleration.
     */
    MAX_DECELERATION,
    /**
     * Indicates a change of the virtual vehicle's maximum forward velocity.
     */
    MAX_FORWARD_VELOCITY,
    /**
     * Indicates a change of the virtual vehicle's maximum reverse velocity.
     */
    MAX_REVERSE_VELOCITY,
    /**
     * Indicates a change of the virtual vehicle's paused setting.
     */
    VEHICLE_PAUSED,
    /**
     * Indicates a change of the virtual vehicle's movement power setting.
     */
    MOVEMENT_POWER,
    /**
     * Indicates a change of the virtual vehicle's operation power setting.
     */
    OPERATION_POWER,
    /**
     * Indicates a change of the virtual vehicle's idle power setting.
     */
    IDLE_POWER,
    /**
     * Indicates a change of the virtual vehicle's velocity history.
     */
    VELOCITY_HISTORY;
  }
}
