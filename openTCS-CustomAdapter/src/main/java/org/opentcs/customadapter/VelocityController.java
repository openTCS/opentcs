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

  public VelocityController(
      int maxAcceleration, int maxDeceleration, int maxFwdVelocity, int maxRevVelocity
  ) {
    this.maxAcceleration = maxAcceleration;
    this.maxDeceleration = maxDeceleration;
    this.maxFwdVelocity = maxFwdVelocity;
    this.maxRevVelocity = maxRevVelocity;
    this.currentVelocity = 0;
  }

  public int getCurrentVelocity() {
    return currentVelocity;
  }

  public void setCurrentVelocity(int velocity) {
    if (velocity > maxFwdVelocity) {
      LOG.warning(
          "Attempted to set velocity above maximum forward velocity. Setting to max forward velocity."
      );
      this.currentVelocity = maxFwdVelocity;
    }
    else if (velocity < -maxRevVelocity) {
      LOG.warning(
          "Attempted to set velocity below maximum reverse velocity. Setting to max reverse velocity."
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
