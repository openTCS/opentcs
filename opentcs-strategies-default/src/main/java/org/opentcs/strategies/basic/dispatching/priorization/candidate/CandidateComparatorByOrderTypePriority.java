// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.priorization.candidate;

import java.util.Comparator;
import java.util.Objects;
import org.opentcs.data.model.AcceptableOrderType;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;

/**
 * Compares {@link AssignmentCandidate}s by the priority of the order type for a given vehicle.
 * Candidates with higher priority (lower integer value) come first.
 */
public class CandidateComparatorByOrderTypePriority
    implements
      Comparator<AssignmentCandidate> {

  /**
   * A key used for selecting this comparator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "BY_ORDER_TYPE_PRIORITY";

  /**
   * Creates a new instance.
   */
  public CandidateComparatorByOrderTypePriority() {
  }

  /**
   * Compares two candidates by the priorities of the vehicle's order types.
   *
   * @param candidate1 The first candidate.
   * @param candidate2 The second candidate.
   * @return the value zero, if the order type of candidate1 and candidate2 have the same priority;
   * a value less than zero, if candidate1 has a lower priority than candidate2;
   * a value greater than zero otherwise.
   * @see Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare(AssignmentCandidate candidate1, AssignmentCandidate candidate2) {
    return Integer.compare(getOrderTypePriority(candidate1), getOrderTypePriority(candidate2));
  }

  private int getOrderTypePriority(AssignmentCandidate candidate) {
    return candidate.getVehicle().getAcceptableOrderTypes().stream()
        .filter(
            orderType -> Objects.equals(
                orderType.getName(), candidate.getTransportOrder().getType()
            )
                || Objects.equals(orderType.getName(), OrderConstants.TYPE_ANY)
        )
        // All types should have precedence over the "any" type.
        .sorted(this::orderTypeAnyLast)
        .findFirst()
        .map(AcceptableOrderType::getPriority)
        .orElse(Integer.MAX_VALUE);
  }

  private int orderTypeAnyLast(AcceptableOrderType orderType1, AcceptableOrderType orderType2) {
    if (Objects.equals(orderType1.getName(), OrderConstants.TYPE_ANY)) {
      return 1;
    }
    if (Objects.equals(orderType2.getName(), OrderConstants.TYPE_ANY)) {
      return -1;
    }
    return 0;
  }
}
