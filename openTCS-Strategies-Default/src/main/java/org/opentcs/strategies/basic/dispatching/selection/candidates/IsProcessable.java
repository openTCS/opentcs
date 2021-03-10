/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.selection.candidates;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.selection.AssignmentCandidateSelectionFilter;

/**
 * Filters assignment candidates with which the transport order is actually processable by the
 * vehicle.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class IsProcessable
    implements AssignmentCandidateSelectionFilter {

  /**
   * Checks processability of transport orders for vehicles.
   */
  private final ProcessabilityChecker processabilityChecker;

  /**
   * Creates a new instance.
   *
   * @param processabilityChecker Checks processability of transport orders for vehicles.
   */
  @Inject
  public IsProcessable(ProcessabilityChecker processabilityChecker) {
    this.processabilityChecker = requireNonNull(processabilityChecker, "processabilityChecker");
  }

  @Override
  public boolean test(AssignmentCandidate candidate) {
    return processabilityChecker.checkProcessability(candidate.getVehicle(),
                                                     candidate.getTransportOrder());
  }
}
