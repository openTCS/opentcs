/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.selection;

import java.util.Collection;
import java.util.function.Function;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;

/**
 * A filter for {@link AssignmentCandidate}s.
 * Returns a collection of reasons for filtering the assignment candidate.
 * If the returned collection is empty, no reason to filter it was encountered.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface AssignmentCandidateSelectionFilter
    extends Function<AssignmentCandidate, Collection<String>> {
}
