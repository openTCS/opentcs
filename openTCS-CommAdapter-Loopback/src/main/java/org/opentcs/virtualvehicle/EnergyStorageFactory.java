/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

/**
 * A factory for creating enegry storages.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface EnergyStorageFactory {

  /**
   * Creates an {@code EnergyStorage} instance.
   * The implementing class that is instanciated is choosen according to the energy storage
   * configuration.
   * Default is {@link org.opentcs.virtualvehicle.StaticEnergyStorage}.
   *
   * @param capacity Capacity of the energy storage in Ws.
   * @return An instance of an {@code EnergyStorage} implementing class.
   */
  EnergyStorage createInstance(double capacity);
}
