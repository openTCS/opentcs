/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import com.google.inject.assistedinject.Assisted;
import javax.inject.Inject;

/**
 * Energy storage that is not affected by charge and discharge operations.
 * The energy level only changes, when set directly via
 * {@link #setEnergy(double) setEnergy} or
 * {@link #setEnergyLevel(int) setEnergyLevel}.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public class StaticEnergyStorage
    implements EnergyStorage {

  /**
   * Capacity of the energy storage in Ws.
   */
  private final double capacity;
  /**
   * Current energy value of the energy storage in W.
   */
  private double energy;

  /**
   * Creates a new <code>StaticEnergyStorage</code>.
   *
   * @param capacity The capacity of this storage in watt seconds [Ws].
   * The value cannot be changed later.
   */
  @Inject
  public StaticEnergyStorage(@Assisted double capacity) {
    this.capacity = capacity; // Initialize the capacity
    energy = capacity;        // Start with a full stoarage
  }

  @Override
  public double getCapacity() {
    return capacity;
  }

  @Override
  public double getEnergy() {
    return energy;
  }

  @Override
  public void setEnergy(double energy) {
    if (energy >= 0 && energy <= capacity) {
      this.energy = energy;
    }
  }

  @Override
  public int getEnergyLevel() {
    return (int) Math.round(100 * energy / capacity);
  }

  @Override
  public void setEnergyLevel(int energyLevel) {
    // Clamp value to [0, 100]
    if (energyLevel < 0 || energyLevel > 100) {
      energyLevel = Math.max(Math.min(energyLevel, 100), 0);
    }
    setEnergy(energyLevel / 100.0 * capacity);
  }

  @Override
  public void charge(double energy) {
    // do nothing
  }

  @Override
  public void charge(double power, int timeMs) {
    // do nothing
  }

  @Override
  public void discharge(double energy) {
    // do nothing
  }

  @Override
  public void discharge(double power, int timeMs) {
    // do nothing
  }
}
