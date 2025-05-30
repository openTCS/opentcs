// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.priorization.transportorder;

import java.util.Comparator;
import org.opentcs.data.order.TransportOrder;

/**
 * Compares {@link TransportOrder}s by age.
 * Note: this comparator imposes orderings that are inconsistent with equals.
 */
public class TransportOrderComparatorByAge
    implements
      Comparator<TransportOrder> {

  /**
   * A key used for selecting this comparator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "BY_AGE";

  /**
   * Creates a new instance.
   */
  public TransportOrderComparatorByAge() {
  }

  /**
   * Compares two orders by their age.
   * Note: this comparator imposes orderings that are inconsistent with equals.
   *
   * @see Comparator#compare(java.lang.Object, java.lang.Object)
   * @param order1 The first order.
   * @param order2 The second order.
   * @return the value zero, if the transport order have the same creation time;
   * a value less than zero, if order1 is older than order2.
   * a value greater than zero otherwise.
   */
  @Override
  public int compare(TransportOrder order1, TransportOrder order2) {
    return order1.getCreationTime().compareTo(order2.getCreationTime());
  }

}
