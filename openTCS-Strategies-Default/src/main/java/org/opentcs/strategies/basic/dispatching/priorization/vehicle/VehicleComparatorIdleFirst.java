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
 * Compares {@link Vehicle}s by their states, ordering IDLE vehicles first.
 * Note: this comparator imposes orderings that are inconsistent with equals.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleComparatorIdleFirst
    implements Comparator<Vehicle> {

  /**
   * A key used for selecting this comparator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "IDLE_FIRST";

  /**
   * Compares two vehicles by their state.
   * Note: this comparator imposes orderings that are inconsistent with equals.
   *
   * @see Comparator#compare(java.lang.Object, java.lang.Object)
   * @param vehicle1 The first vehicle.
   * @param vehicle2 The second vehicle.
   * @return The value zero if vehicle1 and vehicle2 have the same state;
   * a value grater zero, if the state of vehicle1 is idle, unlike vehicle2;
   * a value less than zero otherwise.
   */
  @Override
  public int compare(Vehicle vehicle1, Vehicle vehicle2) {
    if (vehicle1.getState() == vehicle2.getState()) {
      return 0;
    }
    else if (vehicle1.getState() == Vehicle.State.IDLE) {
      return -1;
    }
    else {
      return 1;
    }
  }

}
