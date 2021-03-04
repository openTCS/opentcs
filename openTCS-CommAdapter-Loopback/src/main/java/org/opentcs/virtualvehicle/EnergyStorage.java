/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

/**
 * Interface for implementations of energy storage simulations.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface EnergyStorage {

  /**
   * Returns the capacity of this storage.
   *
   * @return The capacity of this storage in watt seconds [Ws].
   */
  double getCapacity();

  /**
   * Returns the remaining energy of this storage.
   *
   * @return The currently remaining energy of this storage in watt seconds [Ws].
   */
  double getEnergy();

  /**
   * Sets a new energy value.
   *
   * @param energy The new value for the current energy.
   */
  void setEnergy(double energy);

  /**
   * Returns the current energy level.
   *
   * @return The currently remaining energy in percent
   */
  int getEnergyLevel();

  /**
   * Set energy level in percentage of capacity.
   * Invalid values will be automatically clamped.
   * @param energyLevel the new energy level
   */
  void setEnergyLevel(int energyLevel);

  /**
   * Charges this storage.
   *
   * @param energy Charge with a fix amount of energy in watt seconds [Ws]
   */
  void charge(double energy);

  /**
   * Charge with <code>power</code> over the time <code>timeSec</code>.
   *
   * @param power Charge with power in watt [W]
   * @param timeMs Charging time in milli seconds [ms]
   */
  void charge(double power, int timeMs);

  /**
   * Discharges this storage.
   *
   * @param energy Discharge with a fix amount of energy in watt seconds [Ws]
   */
  void discharge(double energy);

  /**
   * Discharge with <code>power</code> over the time <code>timeSec</code>.
   *
   * @param power Charge with power in watt [W]
   * @param timeMs Charging time in milli seconds [ms]
   */
  void discharge(double power, int timeMs);
}
