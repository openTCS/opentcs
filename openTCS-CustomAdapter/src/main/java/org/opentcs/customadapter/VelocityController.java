package org.opentcs.customadapter;

import java.util.logging.Logger;

// VelocityController inner class
public class VelocityController {
  private static final Logger LOG = Logger.getLogger(VelocityController.class.getName());

  private double maxAcceleration;
  private double maxDeceleration;
  private double maxFwdVelocity;
  private double maxRevVelocity;
  private double currentVelocity;

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
      double maxAcceleration, double maxDeceleration, double maxFwdVelocity, double maxRevVelocity
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
  public double getCurrentVelocity() {
    return currentVelocity;
  }

  /**
   * Sets the current velocity of the vehicle.
   *
   * @param velocity The new velocity value to set.
   */
  public void setCurrentVelocity(double velocity) {
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

  public double getMaxAcceleration() {
    return maxAcceleration;
  }

  public void setMaxAcceleration(double maxAcceleration) {
    this.maxAcceleration = maxAcceleration;
  }

  public double getMaxDeceleration() {
    return maxDeceleration;
  }

  public void setMaxDeceleration(double maxDeceleration) {
    this.maxDeceleration = maxDeceleration;
  }

  public double getMaxFwdVelocity() {
    return maxFwdVelocity;
  }

  public void setMaxFwdVelocity(double maxFwdVelocity) {
    this.maxFwdVelocity = maxFwdVelocity;
  }

  public double getMaxRevVelocity() {
    return maxRevVelocity;
  }

  public void setMaxRevVelocity(double maxRevVelocity) {
    this.maxRevVelocity = maxRevVelocity;
  }
}
