/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.selection;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;

/**
 * A collection of {@link AssignmentCandidateSelectionFilter}s.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CompositeAssignmentCandidateSelectionFilter
    implements AssignmentCandidateSelectionFilter {

  /**
   * The {@link AssignmentCandidateSelectionFilter}s.
   */
  private final Set<AssignmentCandidateSelectionFilter> filters;

  @Inject
  public CompositeAssignmentCandidateSelectionFilter(
      Set<AssignmentCandidateSelectionFilter> filters) {
    this.filters = requireNonNull(filters, "filters");
  }

  @Override
  public boolean test(AssignmentCandidate candidate) {
    boolean result = true;
    for (AssignmentCandidateSelectionFilter filter : filters) {
      result &= filter.test(candidate);
    }
    return result;
  }
}
