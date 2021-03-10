/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase;

import java.util.Collection;
import static java.util.Objects.requireNonNull;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;

/**
 * The result of an assignment candidate filter operation.
 *
 * @author Stefan Walter (Fraunhofer IML)
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
