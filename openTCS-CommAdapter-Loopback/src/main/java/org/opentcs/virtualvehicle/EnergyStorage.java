/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import org.opentcs.util.configuration.ConfigurationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstact base class for implementations of energy storage simulations.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public abstract class EnergyStorage {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(EnergyStorage.class);
  /**
   * This class's ConfigurationStore.
   */
  private static final ConfigurationStore configStore
      = ConfigurationStore.getStore(EnergyStorage.class.getName());

  /**
   * Create an {@code EnergyStorage} instance.  
   * The subclass that is instanciated is choosen acording to the energy storage
   * configuration in the {@code ConfigurationStore}. 
   * Default is {@link org.opentcs.virtualvehicle.StaticEnergyStorage}.
   * @param capacity Capacity of the energy storage in Ws
   * @return new instance of an {@code EnergyStorage} subclass
   */
  public static EnergyStorage createInstance(double capacity) {
    String type = configStore.getString("energyStorageType", "StaticEnergyStorage");
    EnergyStorage instance;
    if (type.equals("SimulatingEnergyStorage")) {
      instance = new SimulatingEnergyStorage(capacity);
    }
    else if (type.equals("StaticEnergyStorage")) {
      instance = new StaticEnergyStorage(capacity);
    }
    else {
      log.debug("Unknown EnergyStorage type specified: '" + type + "' Using"
          + "default EnergyStorage instead.");
      instance = new StaticEnergyStorage(capacity);
    }
    return instance;
  }

  /**
   * Like {@link #createInstance(double)}, but using capacity value specified
   * in the {@code ConfigurationStore} or default value if not specified.
   * @return new instance of an {@code EnergyStorage} subclass
   */
  public static EnergyStorage createInstance() {
    double defaultValue = 1000;
    double capacity = configStore.getDouble("energyStorageCapacity", defaultValue);
    if (capacity <= 0) {
      log.debug("Invalid capacity value specified: '" + capacity + "' Using"
          + "default value instead.");
      capacity = defaultValue;
    }
    return createInstance(capacity);
  }

  /**
   * Returns the capacity of this storage.
   * 
   * @return The capacity of this storage in watt seconds [Ws].
   */
  public abstract double getCapacity();

  /**
   * Returns the remaining energy of this storage.
   * 
   * @return The currently remaining energy of this storage in watt seconds [Ws].
   */
  public abstract double getEnergy();

  /**
   * Sets a new energy value.
   * 
   * @param energy The new value for the current energy.
   */
  public abstract void setEnergy(double energy);

  /**
   * Returns the current energy level.
   * 
   * @return The currently remaining energy in percent
   */
  public abstract int getEnergyLevel();

  /**
   * Set energy level in percentage of capacity.
   * Invalid values will be automatically clamped.
   * @param energyLevel the new energy level
   */
  public abstract void setEnergyLevel(int energyLevel);

  /**
   * Charges this storage.
   * 
   * @param energy Charge with a fix amount of energy in watt seconds [Ws]
   */
  public abstract void charge(double energy);

  /**
   * Charge with <code>power</code> over the time <code>timeSec</code>.
   * 
   * @param power Charge with power in watt [W]
   * @param timeMs Charging time in milli seconds [ms]
   */
  public abstract void charge(double power, int timeMs);

  /**
   * Discharges this storage.
   * 
   * @param energy Discharge with a fix amount of energy in watt seconds [Ws]
   */
  public abstract void discharge(double energy);

  /**
   * Discharge with <code>power</code> over the time <code>timeSec</code>.
   * 
   * @param power Charge with power in watt [W]
   * @param timeMs Charging time in milli seconds [ms]
   */
  public abstract void discharge(double power, int timeMs);
}
