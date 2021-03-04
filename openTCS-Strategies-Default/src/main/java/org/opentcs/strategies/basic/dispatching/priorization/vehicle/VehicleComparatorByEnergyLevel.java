/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.priorization.vehicle;

import java.util.Comparator;
import org.opentcs.data.model.Vehicle;

/**
 * Compares {@link Vehicle}s by energy level, sorting higher energy levels up.
 * Note: this comparator imposes orderings that are inconsistent with equals.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleComparatorByEnergyLevel
    implements Comparator<Vehicle> {

  /**
   * A key used for selecting this comparator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "BY_ENERGY_LEVEL";

  /**
   * Compares two vehicles by their energy level.
   * Note: this comparator imposes orderings that are inconsistent with equals.
   *
   * @see Comparator#compare(java.lang.Object, java.lang.Object)
   * @param vehicle1 The first vehicle.
   * @param vehicle2 The second vehicel.
   * @return the value 0 if vehicle1 and vehicle2 have the same energy level;
   * a value less than 0 if vehicle1 has a higher energy level than vehicle2;
   * and a value greater than 0 otherwise.
   */
  @Override
  public int compare(Vehicle vehicle1, Vehicle vehicle2) {
    return -Integer.compare(vehicle1.getEnergyLevel(), vehicle2.getEnergyLevel());
  }

}
