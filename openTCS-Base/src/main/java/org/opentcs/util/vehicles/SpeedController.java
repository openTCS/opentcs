/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.vehicles;

import java.util.Objects;

/**
 * SpeedController reduces the speed if too few following points are available.
 * If a critical level is reached, the speed will be reduced, at danger level
 * the speed will be set to zero. Otherwise the desired speed will be set to max
 * speed.
 * 
 * <p>
 * Call {@link #setNextFree(int)} in the driver to set the following free points
 * and calculate the new speed. Call {@link #getCurrentSpeed()} to get the
 * resulting speed.
 * </p>
 *
 * @author Volkmar Pontow (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 * @param <S> The class used for representing speed values.
 */
public class SpeedController<S> {

  /**
   * Identifies the vehicle.
   */
  private final String vehicle;
  /**
   * Maximum speed.
   */
  private final S maxSpeed;
  /**
   * Reduced speed for critical level.
   */
  private final S reducedSpeed;
  /**
   * Speed for danger level.
   */
  private final S noSpeed;
  /**
   * Threshold/number of free points at which to stop the vehicle.
   */
  private final int levelDanger;
  /**
   * Threshold/number of free points at which to use reduced speed.
   */
  private final int levelCritical;
  /**
   * Current number of free points.
   */
  private int nextFree;
  /**
   * The current speed.
   */
  private S currentSpeed;

  /**
   * Creates a new instance.
   * 
   * @param vehicle Identifies the vehicle.
   * @param maxSpeed Maximum speed.
   * @param reducedSpeed Reduced speed for critical level.
   * @param noSpeed Speed for danger level.
   * @param levelDanger Threshold/number of free points at which to stop the
   * vehicle.
   * @param levelCritical Threshold/number of free points at which to use
   * reduced speed.
   */
  public SpeedController(final String vehicle,
                         final S maxSpeed,
                         final S reducedSpeed,
                         final S noSpeed,
                         final int levelDanger,
                         final int levelCritical) {
    this.vehicle = Objects.requireNonNull(vehicle, "vehicle is null");
    this.maxSpeed = Objects.requireNonNull(maxSpeed, "maxSpeed is null");
    this.reducedSpeed = Objects.requireNonNull(reducedSpeed,
                                               "reducedSpeed is null");
    this.noSpeed = Objects.requireNonNull(noSpeed, "noSpeed is null");
    this.levelDanger = levelDanger;
    this.levelCritical = levelCritical;
    this.currentSpeed = noSpeed;
  }

  /**
   * Returns the vehicle's ID.
   *
   * @return The vehicle's ID.
   */
  public String getVehicle() {
    return vehicle;
  }

  /**
   * Returns the maximum speed.
   *
   * @return The maximum speed.
   */
  public S getMaxSpeed() {
    return maxSpeed;
  }

  /**
   * Returns the reduced speed.
   *
   * @return The reduced speed.
   */
  public S getReducedSpeed() {
    return reducedSpeed;
  }

  /**
   * Returns the threshold/number of free points at which the vehicle must stop.
   *
   * @return The threshold/number of free points at which the vehicle must stop.
   */
  public int getLevelDanger() {
    return levelDanger;
  }

  /**
   * Returns the threshold/number of free points at which the vehicle must
   * reduce speed.
   *
   * @return The threshold/number of free points at which the vehicle must
   * reduce speed.
   */
  public int getLevelCritical() {
    return levelCritical;
  }

  /**
   * Returns the number of free points.
   *
   * @return The number of free points.
   */
  public int getNextFree() {
    return nextFree;
  }

  /**
   * Returns the computed speed.
   *
   * @return The computed speed.
   */
  public S getCurrentSpeed() {
    return currentSpeed;
  }

  /**
   * Sets the number of free points.
   *
   * @param nextFree number of free points
   */
  public void setNextFree(int nextFree) {
    if (this.nextFree != nextFree) {
      if (nextFree <= levelDanger) {
        currentSpeed = noSpeed;
      }
      else if (nextFree <= levelCritical) {
        currentSpeed = reducedSpeed;
      }
      else {
        currentSpeed = maxSpeed;
      }
    }
    this.nextFree = nextFree;
  }

  @Override
  public String toString() {
    return vehicle + ": speed=" + getCurrentSpeed()
        + " nextFree=" + getNextFree()
        + " maxSpeed=" + getMaxSpeed()
        + " reducedSpeed=" + getReducedSpeed()
        + " levelDanger=" + getLevelDanger()
        + " levelCritical=" + getLevelCritical();
  }

  /**
   * Returns a shorter String representation containing only the computed speed
   * and number of free points.
   *
   * @return A shorter String representation containing only the computed speed
   * and number of free points.
   */
  public String toStringShort() {
    return vehicle + ": speed=" + getCurrentSpeed()
        + " nextFree=" + getNextFree();
  }
}
