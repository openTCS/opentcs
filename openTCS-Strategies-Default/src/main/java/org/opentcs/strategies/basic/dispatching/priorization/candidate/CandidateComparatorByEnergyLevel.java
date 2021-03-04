/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.priorization.candidate;

import java.util.Comparator;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorByEnergyLevel;

/**
 * Compares {@link AssignmentCandidate}s by the energy level of their vehicles.
 * Note: this comparator imposes orderings that are inconsistent with equals.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CandidateComparatorByEnergyLevel
    implements Comparator<AssignmentCandidate> {

  /**
   * A key used for selecting this comparator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "BY_ENERGY_LEVEL";

  private final Comparator<Vehicle> delegate = new VehicleComparatorByEnergyLevel();

  /**
   * Compares two candidates by the energy level of their vehicles.
   * Note: this comparator imposes orderings that are inconsistent with equals.
   *
   * @see Comparator#compare(java.lang.Object, java.lang.Object)
   * @param candidate1 The first candidate.
   * @param candidate2 The second candidate.
   * @return the value 0 if candidate1 and candidate2 have the same energy level;
   * a value less than 0 if candidate1 has a higher energy level than candidate2;
   * and a value greater than 0 otherwise.
   */
  @Override
  public int compare(AssignmentCandidate candidate1, AssignmentCandidate candidate2) {
    return delegate.compare(candidate1.getVehicle(), candidate2.getVehicle());
  }

}
