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
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorByName;

/**
 * Compares {@link AssignmentCandidate}s by name of the vehicle.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CandidateComparatorByVehicleName
    implements Comparator<AssignmentCandidate> {

  /**
   * A key used for selecting this comparator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "BY_VEHICLE_NAME";

  private final Comparator<Vehicle> delegate = new VehicleComparatorByName();

  @Override
  public int compare(AssignmentCandidate candidate1, AssignmentCandidate candidate2) {
    return delegate.compare(candidate1.getVehicle(), candidate2.getVehicle());
  }

}
