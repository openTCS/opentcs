// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.priorization.candidate;

import java.util.Comparator;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.priorization.transportorder.TransportOrderComparatorByDeadline;

/**
 * Compares {@link AssignmentCandidate}s by deadline of the order.
 * Note: this comparator imposes orderings that are inconsistent with equals.
 */
public class CandidateComparatorByDeadline
    implements
      Comparator<AssignmentCandidate> {

  /**
   * A key used for selecting this comparator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "BY_DEADLINE";

  private final Comparator<TransportOrder> delegate = new TransportOrderComparatorByDeadline();

  /**
   * Creates a new instance.
   */
  public CandidateComparatorByDeadline() {
  }

  /**
   * Compares two candidates by the deadline of their transport order.
   * Note: this comparator imposes orderings that are inconsistent with equals.
   *
   * @see Comparator#compare(java.lang.Object, java.lang.Object)
   * @param candidate1 The first candidate
   * @param candidate2 The second candidate
   * @return the value 0 if candidate1 and candidate2 have the same deadline;
   * a value less than 0 if candidate1 has an earlier deadline than candidate2;
   * and a value greater than 0 otherwise.
   */
  @Override
  public int compare(AssignmentCandidate candidate1, AssignmentCandidate candidate2) {
    return delegate.compare(candidate1.getTransportOrder(), candidate2.getTransportOrder());
  }

}
