package org.opentcs.customadapter;

import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleProcessModel;

public class CustomProcessModel
    extends
      VehicleProcessModel {

  private final String loadOperation = "Load";
  private final String unloadOperation = "Unload";
  /**
   * The velocity controller for calculating the simulated vehicle's velocity and current position.
   */
  private final VelocityController velocityController;

  /**
   * Constructs a new CustomProcessModel object with the specified attachedVehicle.
   *
   * @param attachedVehicle the vehicle object that this process model is attached to
   */
  public CustomProcessModel(
      Vehicle attachedVehicle
  ) {
    super(attachedVehicle);
    this.velocityController = new VelocityController(
        1.6,
        -1.6,
        attachedVehicle.getMaxReverseVelocity(),
        attachedVehicle.getMaxVelocity()
    );
  }

  public String getLoadOperation() {
    return loadOperation;
  }

  public String getUnloadOperation() {
    return unloadOperation;
  }

  /**
   * Returns the maximum deceleration.
   *
   * @return The maximum deceleration
   */
  public synchronized double getMaxDecceleration() {
    return velocityController.getMaxDeceleration();
  }

  /**
   * Sets the maximum deceleration.
   *
   * @param maxDeceleration The new maximum deceleration
   */
  public synchronized void setMaxDeceleration(double maxDeceleration) {
    double oldValue = velocityController.getMaxDeceleration();
    velocityController.setMaxDeceleration(maxDeceleration);

    getPropertyChangeSupport().firePropertyChange(
        Attribute.DECELERATION.name(),
        oldValue,
        maxDeceleration
    );
  }

  /**
   * Returns the maximum acceleration.
   *
   * @return The maximum acceleration
   */
  public synchronized double getMaxAcceleration() {
    return velocityController.getMaxAcceleration();
  }

  /**
   * Sets the maximum acceleration.
   *
   * @param maxAcceleration The new maximum acceleration
   */
  public synchronized void setMaxAcceleration(double maxAcceleration) {
    double oldValue = velocityController.getMaxAcceleration();
    velocityController.setMaxAcceleration(maxAcceleration);

    getPropertyChangeSupport().firePropertyChange(
        Attribute.ACCELERATION.name(),
        oldValue,
        maxAcceleration
    );
  }

  /**
   * Returns the maximum reverse velocity.
   *
   * @return The maximum reverse velocity.
   */
  public synchronized double getMaxRevVelocity() {
    return velocityController.getMaxRevVelocity();
  }

  /**
   * Sets the maximum reverse velocity.
   *
   * @param maxRevVelocity The new maximum reverse velocity
   */
  public synchronized void setMaxRevVelocity(double maxRevVelocity) {
    double oldValue = velocityController.getMaxRevVelocity();
    velocityController.setMaxRevVelocity(maxRevVelocity);

    getPropertyChangeSupport().firePropertyChange(
        Attribute.MAX_REVERSE_VELOCITY.name(),
        oldValue,
        maxRevVelocity
    );
  }

  /**
   * Returns the maximum forward velocity.
   *
   * @return The maximum forward velocity.
   */
  public synchronized double getMaxFwdVelocity() {
    return velocityController.getMaxFwdVelocity();
  }

  /**
   * Sets the maximum forward velocity.
   *
   * @param maxFwdVelocity The new maximum forward velocity.
   */
  public synchronized void setMaxFwdVelocity(double maxFwdVelocity) {
    double oldValue = velocityController.getMaxFwdVelocity();
    velocityController.setMaxFwdVelocity(maxFwdVelocity);

    getPropertyChangeSupport().firePropertyChange(
        Attribute.MAX_FORWARD_VELOCITY.name(),
        oldValue,
        maxFwdVelocity
    );
  }

  /**
   * The Attribute enumeration.
   */
  public enum Attribute {
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
  }
}
