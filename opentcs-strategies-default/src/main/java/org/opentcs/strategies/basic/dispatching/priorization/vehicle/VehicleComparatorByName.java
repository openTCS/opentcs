// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.priorization.vehicle;

import java.util.Comparator;
import org.opentcs.data.model.Vehicle;

/**
 * Compares {@link Vehicle}s by their names.
 */
public class VehicleComparatorByName
    implements
      Comparator<Vehicle> {

  /**
   * A key used for selecting this comparator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "BY_NAME";

  /**
   * Creates a new instance.
   */
  public VehicleComparatorByName() {
  }

  @Override
  public int compare(Vehicle vehicle1, Vehicle vehicle2) {
    return vehicle1.getName().compareTo(vehicle2.getName());
  }

}
