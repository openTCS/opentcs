/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.assignment.priorization;

import java.util.Comparator;
import org.opentcs.data.order.TransportOrder;

/**
 * Compares {@link TransportOrder}s by age.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrderComparatorByAge
    implements Comparator<TransportOrder> {

  /**
   * A key used for selecting this comparator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "BY_AGE";

  @Override
  public int compare(TransportOrder order1, TransportOrder order2) {
    return Long.compare(order1.getCreationTime(), order2.getCreationTime());
  }

}
