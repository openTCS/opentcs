// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.priorization.transportorder;

import java.util.Comparator;
import org.opentcs.data.order.TransportOrder;

/**
 * Compares {@link TransportOrder}s by their names.
 */
public class TransportOrderComparatorByName
    implements
      Comparator<TransportOrder> {

  /**
   * A key used for selecting this comparator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "BY_NAME";

  /**
   * Creates a new instance.
   */
  public TransportOrderComparatorByName() {
  }

  @Override
  public int compare(TransportOrder order1, TransportOrder order2) {
    return order1.getName().compareTo(order2.getName());
  }

}
