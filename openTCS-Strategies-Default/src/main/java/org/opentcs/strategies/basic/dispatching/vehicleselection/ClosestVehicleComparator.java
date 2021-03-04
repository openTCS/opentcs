/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.vehicleselection;

import java.util.Comparator;

/**
 * Compares {@link VehicleCandidate}s by routing costs, then by energy level, and eventually by
 * vehicle name.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ClosestVehicleComparator
    implements Comparator<VehicleCandidate> {

  @Override
  public int compare(VehicleCandidate candidate1, VehicleCandidate candidate2) {
    int result;
    // Lower routing costs are better.
    result = Long.compare(candidate1.getCosts(), candidate2.getCosts());
    if (result != 0) {
      return result;
    }
    // Higher energy level is better.
    result = -Integer.compare(candidate1.getVehicle().getEnergyLevel(),
                              candidate2.getVehicle().getEnergyLevel());
    if (result != 0) {
      return result;
    }
    // If they are still equal, just sort them by their names.
    return candidate1.getVehicle().getName().compareTo(candidate2.getVehicle().getName());
  }

}
