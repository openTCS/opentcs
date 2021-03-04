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
 * A simple implementation of a linear energy storage model.
 *
 * @author Hubert Buechter (Fraunhofer IML)
 */
public class SimulatingEnergyStorage
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
   * Creates a new <code>SimulatingEnergyStorage</code>.
   *
   * @param capacity The capacity of this storage in watt seconds [Ws].
   * The value cannot be changed later.
   */
  @Inject
  public SimulatingEnergyStorage(@Assisted double capacity) {
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
    this.energy += energy;

    // keep the energy in range in order to minimize exception handling
    if (this.energy > capacity) {
      this.energy = capacity;
    }
    else if (this.energy < 0) {
      this.energy = 0;
    }
  }

  @Override
  public void charge(double power, int timeMs) {
    charge(power * timeMs / 1000);
  }

  @Override
  public void discharge(double energy) {
    charge(-energy);
  }

  @Override
  public void discharge(double power, int timeMs) {
    charge(-power * timeMs / 1000);
  }
}
