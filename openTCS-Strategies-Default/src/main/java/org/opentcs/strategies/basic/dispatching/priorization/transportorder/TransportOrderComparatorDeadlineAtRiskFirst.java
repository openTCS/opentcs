/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.priorization.transportorder;

import java.util.Comparator;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;

/**
 * Compares {@link TransportOrder}s by their deadlines, ordering those with a deadline at risk
 * first.
 * Note: this comparator imposes orderings that are inconsistent with equals.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class TransportOrderComparatorDeadlineAtRiskFirst
    implements Comparator<TransportOrder> {

  /**
   * A key used for selecting this comparator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "DEADLINE_AT_RISK_FIRST";

  /**
   * The time window (in ms) before its deadline in which an order becomes urgent.
   */
  private final long deadlineAtRiskPeriod;

  @Inject
  public TransportOrderComparatorDeadlineAtRiskFirst(DefaultDispatcherConfiguration configuration) {
    requireNonNull(configuration, "configuration");

    this.deadlineAtRiskPeriod = configuration.deadlineAtRiskPeriod();
  }

  /**
   * Compares two orders by their deadline's criticality.
   * Note: this comparator imposes orderings that are inconsistent with equals.
   *
   * @see Comparator#compare(java.lang.Object, java.lang.Object)
   * @param order1 The first order.
   * @param order2 The second order.
   * @return the value 0 if the deadlines of order1 and order2 are both at risk or not,
   * a value less than 0 if only the deadline of order1 is at risk
   * and a value greater than 0 in all other cases.
   */
  @Override
  public int compare(TransportOrder order1, TransportOrder order2) {
    boolean order1AtRisk = deadlineAtRisk(order1);
    boolean order2AtRisk = deadlineAtRisk(order2);

    if (order1AtRisk == order2AtRisk) {
      return 0;
    }
    else if (order1AtRisk) {
      return -1;
    }
    else {
      return 1;
    }
  }

  private boolean deadlineAtRisk(TransportOrder order) {
    return order.getDeadline() - deadlineAtRiskPeriod < System.currentTimeMillis();
  }

}
