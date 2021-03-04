/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.priorization.candidate;

import java.util.Comparator;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.priorization.transportorder.TransportOrderComparatorByName;

/**
 * Compares {@link AssignmentCandidate}s by name of the order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CandidateComparatorByOrderName
    implements Comparator<AssignmentCandidate> {

  /**
   * A key used for selecting this comparator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "BY_ORDER_NAME";

  private final Comparator<TransportOrder> delegate = new TransportOrderComparatorByName();

  @Override
  public int compare(AssignmentCandidate candidate1, AssignmentCandidate candidate2) {
    return delegate.compare(candidate1.getTransportOrder(), candidate2.getTransportOrder());
  }

}
