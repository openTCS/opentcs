/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import javax.annotation.Nonnull;
import org.opentcs.common.LoopbackAdapterConstants;
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
   * Indicates which operation is a loading operation.
   */
  private final String loadOperation;
  /**
   * Indicates which operation is an unloading operation.
   */
  private final String unloadOperation;
  /**
   * The time needed for executing operations.
   */
  private int operatingTime;
  /**
   * The velocity controller for calculating the simulated vehicle's velocity and current position.
   */
  private final VelocityController velocityController;
  /**
   * Keeps a log of recent velocity values.
   */
  private final VelocityHistory velocityHistory = new VelocityHistory(100, 10);

  public LoopbackVehicleModel(Vehicle attachedVehicle) {
    super(attachedVehicle);
    this.velocityController = new VelocityController(parseDeceleration(attachedVehicle),
                                                     parseAcceleration(attachedVehicle),
                                                     attachedVehicle.getMaxReverseVelocity(),
                                                     attachedVehicle.getMaxVelocity());
    this.operatingTime = parseOperatingTime(attachedVehicle);
    this.loadOperation
        = attachedVehicle.getProperty(LoopbackAdapterConstants.PROPKEY_LOAD_OPERATION);
    this.unloadOperation
        = attachedVehicle.getProperty(LoopbackAdapterConstants.PROPKEY_UNLOAD_OPERATION);
  }

  public String getLoadOperation() {
    return this.loadOperation;
  }

  public String getUnloadOperation() {
    return this.unloadOperation;
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
   * Returns the default operating time.
   *
   * @return The default operating time
   */
  public synchronized int getOperatingTime() {
    return operatingTime;
  }

  /**
   * Sets the default operating time.
   *
   * @param defaultOperatingTime The new default operating time
   */
  public synchronized void setOperatingTime(int defaultOperatingTime) {
    int oldValue = this.operatingTime;
    this.operatingTime = defaultOperatingTime;

    getPropertyChangeSupport().firePropertyChange(Attribute.OPERATING_TIME.name(),
                                                  oldValue,
                                                  defaultOperatingTime);
  }

  /**
   * Returns the maximum deceleration.
   *
   * @return The maximum deceleration
   */
  public synchronized int getMaxDecceleration() {
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

    getPropertyChangeSupport().firePropertyChange(Attribute.DECELERATION.name(),
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

    getPropertyChangeSupport().firePropertyChange(Attribute.ACCELERATION.name(),
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

  private int parseOperatingTime(Vehicle vehicle) {
    String opTime = vehicle.getProperty(LoopbackAdapterConstants.PROPKEY_OPERATING_TIME);
    // Ensure it's a positive value.
    return Math.max(Parsers.tryParseString(opTime, 5000), 1);
  }

  /**
   * Gets the maximum acceleration. If the user did not specify any, 1000(m/s²) is returned.
   *
   * @param vehicle the vehicle
   * @return the maximum acceleration.
   */
  private int parseAcceleration(Vehicle vehicle) {
    String acceleration = vehicle.getProperty(LoopbackAdapterConstants.PROPKEY_ACCELERATION);
    // Ensure it's a positive value.
    return Math.max(Parsers.tryParseString(acceleration, 500), 1);
  }

  /**
   * Gets the maximum decceleration. If the user did not specify any, 1000(m/s²) is returned.
   *
   * @param vehicle the vehicle
   * @return the maximum decceleration.
   */
  private int parseDeceleration(Vehicle vehicle) {
    String deceleration = vehicle.getProperty(LoopbackAdapterConstants.PROPKEY_DECELERATION);
    // Ensure it's a negative value.
    return Math.min(Parsers.tryParseString(deceleration, -500), -1);
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
     * Indicates a change of the virtual vehicle's default operating time.
     */
    OPERATING_TIME,
    /**
     * Indicates a change of the virtual vehicle's maximum acceleration.
     */
    ACCELERATION,
    /**
     * Indicates a change of the virtual vehicle's maximum deceleration.
     */
    DECELERATION,
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
     * Indicates a change of the virtual vehicle's velocity history.
     */
    VELOCITY_HISTORY,
  }
}
