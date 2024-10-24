// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.selection;

import java.util.Collection;
import java.util.function.Function;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;

/**
 * A filter for {@link AssignmentCandidate}s.
 * Returns a collection of reasons for filtering the assignment candidate.
 * If the returned collection is empty, no reason to filter it was encountered.
 */
public interface AssignmentCandidateSelectionFilter
    extends
      Function<AssignmentCandidate, Collection<String>> {
}
