// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;

/**
 * The result of an assignment candidate filter operation.
 */
public class CandidateFilterResult {

  private final AssignmentCandidate candidate;

  private final Collection<String> filterReasons;

  public CandidateFilterResult(AssignmentCandidate candidate, Collection<String> filterReasons) {
    this.candidate = requireNonNull(candidate, "candidate");
    this.filterReasons = requireNonNull(filterReasons, "filterReasons");
  }

  public AssignmentCandidate getCandidate() {
    return candidate;
  }

  public Collection<String> getFilterReasons() {
    return filterReasons;
  }

  public boolean isFiltered() {
    return !filterReasons.isEmpty();
  }

  public OrderFilterResult toFilterResult() {
    return new OrderFilterResult(candidate.getTransportOrder(), filterReasons);
  }
}
