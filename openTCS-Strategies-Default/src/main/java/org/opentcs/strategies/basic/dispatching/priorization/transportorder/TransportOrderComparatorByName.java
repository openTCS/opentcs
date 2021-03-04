/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.priorization.transportorder;

import java.util.Comparator;
import org.opentcs.data.order.TransportOrder;

/**
 * Compares {@link TransportOrder}s by their names.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrderComparatorByName
    implements Comparator<TransportOrder> {

  /**
   * A key used for selecting this comparator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "BY_NAME";

  @Override
  public int compare(TransportOrder order1, TransportOrder order2) {
    return order1.getName().compareTo(order2.getName());
  }

}
