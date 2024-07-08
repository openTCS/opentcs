package org.opentcs.customadapter;

import java.util.logging.Logger;

// VelocityController inner class
public class VelocityController {
  private static final Logger LOG = Logger.getLogger(VelocityController.class.getName());

  private int maxAcceleration;
  private int maxDeceleration;
  private int maxFwdVelocity;
  private int maxRevVelocity;
  private int currentVelocity;

  /**
   * The VelocityController class represents a controller for managing the velocity of a vehicle.
   * It allows setting and retrieving the current velocity, as well as configuring the
   * maximum acceleration,
   * maximum deceleration, maximum forward velocity, and maximum reverse velocity.
   * 
   * @param maxAcceleration Max Acceleration of vehicle.
   * @param maxDeceleration Max Deceleration of vehicle.
   * @param maxFwdVelocity Max Forward Velocity of vehicle.
   * @param maxRevVelocity Max Back Velocity of vehicle.
   */
  public VelocityController(
      int maxAcceleration, int maxDeceleration, int maxFwdVelocity, int maxRevVelocity
  ) {
    this.maxAcceleration = maxAcceleration;
    this.maxDeceleration = maxDeceleration;
    this.maxFwdVelocity = maxFwdVelocity;
    this.maxRevVelocity = maxRevVelocity;
    this.currentVelocity = 0;
  }

  /**
   * Retrieves the current velocity of the vehicle.
   *
   * @return The current velocity of the vehicle.
   */
  public int getCurrentVelocity() {
    return currentVelocity;
  }

  /**
   * Sets the current velocity of the vehicle.
   *
   * @param velocity The new velocity value to set.
   */
  public void setCurrentVelocity(int velocity) {
    if (velocity > maxFwdVelocity) {
      LOG.warning(
          "Attempted to set velocity above maximum forward velocity. Setting to max forward "
              + "velocity."
      );
      this.currentVelocity = maxFwdVelocity;
    }
    else if (velocity < -maxRevVelocity) {
      LOG.warning(
          "Attempted to set velocity below maximum reverse velocity. Setting to max reverse "
              + "velocity."
      );
      this.currentVelocity = -maxRevVelocity;
    }
    else {
      this.currentVelocity = velocity;
    }
  }

  public int getMaxAcceleration() {
    return maxAcceleration;
  }

  public void setMaxAcceleration(int maxAcceleration) {
    this.maxAcceleration = maxAcceleration;
  }

  public int getMaxDeceleration() {
    return maxDeceleration;
  }

  public void setMaxDeceleration(int maxDeceleration) {
    this.maxDeceleration = maxDeceleration;
  }

  public int getMaxFwdVelocity() {
    return maxFwdVelocity;
  }

  public void setMaxFwdVelocity(int maxFwdVelocity) {
    this.maxFwdVelocity = maxFwdVelocity;
  }

  public int getMaxRevVelocity() {
    return maxRevVelocity;
  }

  public void setMaxRevVelocity(int maxRevVelocity) {
    this.maxRevVelocity = maxRevVelocity;
  }
}
