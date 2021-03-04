/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;
import static org.opentcs.util.Assertions.checkArgument;
import static org.opentcs.util.Assertions.checkInRange;

/**
 * A transfer object describing a block in the plant model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * The vehicle's length (in mm).
   */
  private int length = 1000;
  /**
   * The energy level value at/below which the vehicle should be recharged.
   */
  private int energyLevelCritical = 30;
  /**
   * The energy level value at/above which the vehicle can be dispatched again when charging.
   */
  private int energyLevelGood = 90;
  /**
   * The vehicle's maximum velocity (in mm/s).
   */
  private int maxVelocity = 1000;
  /**
   * The vehicle's maximum reverse velocity (in mm/s).
   */
  private int maxReverseVelocity = 1000;

  /**
   * Creates a new instance.
   *
   * @param name The name of this vehicle.
   */
  public VehicleCreationTO(@Nonnull String name) {
    super(name);
  }

  /**
   * Sets the name of this vehicle.
   *
   * @param name The new name.
   * @return The modified vehicle.
   */
  @Nonnull
  @Override
  public VehicleCreationTO setName(@Nonnull String name) {
    return (VehicleCreationTO) super.setName(name);
  }

  /**
   * Sets the properties of this vehicle.
   *
   * @param properties The new properties.
   * @return The modified vehicle.
   */
  @Nonnull
  @Override
  public VehicleCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (VehicleCreationTO) super.setProperties(properties);
  }

  /**
   * Sets a single property of this vehicle.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified vehicle.
   */
  @Nonnull
  @Override
  public VehicleCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (VehicleCreationTO) super.setProperty(key, value);
  }

  /**
   * Returns the vehicle's length (in mm).
   *
   * @return The vehicle's length (in mm).
   */
  public int getLength() {
    return length;
  }

  /**
   * Sets the vehicle's length (in mm).
   *
   * @param length The new length. Must be at least 1.
   * @throws IllegalArgumentException If {@code newLength} is less than 1.
   * @return The modified vehicle.
   */
  @Nonnull
  public VehicleCreationTO setLength(int length) {
    checkArgument(length >= 1, "length must be at least 1: " + length);
    this.length = length;
    return this;
  }

  /**
   * Returns this vehicle's critical energy level (in percent of the maximum).
   * The critical energy level is the one at/below which the vehicle should be recharged.
   *
   * @return This vehicle's critical energy level.
   */
  public int getEnergyLevelCritical() {
    return energyLevelCritical;
  }

  /**
   * Sets this vehicle's critical energy level (in percent of the maximum).
   * The critical energy level is the one at/below which the vehicle should be recharged.
   *
   * @param energyLevelCritical The new critical energy level. Must not be smaller than 0 or
   * greater than 100.
   * @return The modified vehicle.
   */
  @Nonnull
  public VehicleCreationTO setEnergyLevelCritical(int energyLevelCritical) {
    checkInRange(energyLevelCritical, 0, 100);
    this.energyLevelCritical = energyLevelCritical;
    return this;
  }

  /**
   * Returns this vehicle's good energy level (in percent of the maximum).
   * The good energy level is the one at/above which the vehicle can be dispatched again when
   * charging.
   *
   * @return This vehicle's good energy level.
   */
  public int getEnergyLevelGood() {
    return energyLevelGood;
  }

  public int getMaxVelocity() {
    return maxVelocity;
  }

  public VehicleCreationTO setMaxVelocity(int maxVelocity) {
    this.maxVelocity = checkInRange(maxVelocity, 0, Integer.MAX_VALUE);
    return this;
  }

  public int getMaxReverseVelocity() {
    return maxReverseVelocity;
  }

  public VehicleCreationTO setMaxReverseVelocity(int maxReverseVelocity) {
    this.maxReverseVelocity = checkInRange(maxReverseVelocity, 0, Integer.MAX_VALUE);
    return this;
  }

  /**
   * Sets this vehicle's good energy level (in percent of the maximum).
   * The good energy level is the one at/above which the vehicle can be dispatched again when
   * charging.
   *
   * @param energyLevelGood The new good energy level. Must not be smaller than 0 or greater than
   * 100.
   * @return The modified vehicle.
   */
  @Nonnull
  public VehicleCreationTO setEnergyLevelGood(int energyLevelGood) {
    checkInRange(energyLevelGood, 0, 100);
    this.energyLevelGood = energyLevelGood;
    return this;
  }
}
