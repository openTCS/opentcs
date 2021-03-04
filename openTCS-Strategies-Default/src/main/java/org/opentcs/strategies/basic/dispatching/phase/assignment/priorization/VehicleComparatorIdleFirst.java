/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.assignment.priorization;

import java.util.Comparator;
import org.opentcs.data.model.Vehicle;

/**
 * Compares {@link Vehicle}s by energy level.
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
