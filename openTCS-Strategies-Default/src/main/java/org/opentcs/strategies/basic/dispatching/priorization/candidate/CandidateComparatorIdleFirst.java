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
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorIdleFirst;

/**
 * Compares {@link AssignmentCandidate}s by vehicles' states, ordering IDLE vehicles first.
 * Note: this comparator imposes orderings that are inconsistent with equals.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CandidateComparatorIdleFirst
    implements Comparator<AssignmentCandidate> {

  /**
   * A key used for selecting this comparator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "IDLE_FIRST";

  private final Comparator<Vehicle> delegate = new VehicleComparatorIdleFirst();

  /**
   * Compares two candidates by the state of their vehicles.
   * Note: this comparator imposes orderings that are inconsistent with equals.
   *
   * @see Comparator#compare(java.lang.Object, java.lang.Object)
   * @param candidate1 The first candiate.
   * @param candidate2 The second candidate.
   * @return The value zero if the vehicles of candidate1 and candidate2 have the same state;
   * a value grater zero, if the vehicle state of candidate1 is idle, unlike candidate2;
   * a value less than zero otherwise.
   */
  @Override
  public int compare(AssignmentCandidate candidate1, AssignmentCandidate candidate2) {
    return delegate.compare(candidate1.getVehicle(), candidate2.getVehicle());
  }

}
