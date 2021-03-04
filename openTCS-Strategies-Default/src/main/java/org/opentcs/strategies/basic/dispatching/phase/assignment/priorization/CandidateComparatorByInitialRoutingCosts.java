/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.assignment.priorization;

import java.util.Comparator;
import org.opentcs.strategies.basic.dispatching.phase.assignment.AssignmentCandidate;

/**
 * Compares {@link AssignmentCandidate}s by routing costs.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CandidateComparatorByInitialRoutingCosts
    implements Comparator<AssignmentCandidate> {

  /**
   * A key used for selecting this comparator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "BY_INITIAL_ROUTING_COSTS";

  @Override
  public int compare(AssignmentCandidate candidate1, AssignmentCandidate candidate2) {
    // Lower routing costs are better.
    return Long.compare(candidate1.getInitialRoutingCosts(), candidate2.getInitialRoutingCosts());
  }

}
