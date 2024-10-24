// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.selection.candidates;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.selection.AssignmentCandidateSelectionFilter;

/**
 * A collection of {@link AssignmentCandidateSelectionFilter}s.
 */
public class CompositeAssignmentCandidateSelectionFilter
    implements
      AssignmentCandidateSelectionFilter {

  /**
   * The {@link AssignmentCandidateSelectionFilter}s.
   */
  private final Set<AssignmentCandidateSelectionFilter> filters;

  @Inject
  public CompositeAssignmentCandidateSelectionFilter(
      Set<AssignmentCandidateSelectionFilter> filters
  ) {
    this.filters = requireNonNull(filters, "filters");
  }

  @Override
  public Collection<String> apply(AssignmentCandidate candidate) {
    return filters.stream()
        .flatMap(filter -> filter.apply(candidate).stream())
        .collect(Collectors.toList());
  }
}
