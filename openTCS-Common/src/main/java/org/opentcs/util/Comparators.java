/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import java.util.Comparator;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Some commonly used comparator implementations.
 */
public final class Comparators {

  /**
   * Prevents undesired instantiation.
   */
  private Comparators() {
  }

  /**
   * Returns a comparator for sorting <code>TCSObject</code>s lexicographically by their names.
   *
   * @return A comparator for sorting <code>TCSObject</code>s lexicographically by their names.
   */
  public static Comparator<TCSObject<?>> objectsByName() {
    return Comparator.comparing(TCSObject::getName);
  }

  /**
   * Returns a comparator for sorting <code>TCSObjectReference</code>s lexicographically by their
   * names.
   *
   * @return A comparator for sorting <code>TCSObjectReference</code>s lexicographically by their
   * names.
   */
  public static Comparator<TCSObjectReference<?>> referencesByName() {
    return Comparator.comparing(TCSObjectReference::getName);
  }

  /**
   * A comparator for sorting transport orders by their deadlines, with the most urgent ones coming
   * first.
   * Transport orders are sorted by their deadlines first; if two orders have exactly the same
   * deadline, they are sorted by their (unique) creation times, with the older one coming first.
   *
   * @return A comparator for sorting transport orders by their deadlines.
   */
  public static Comparator<TransportOrder> ordersByDeadline() {
    return Comparator.comparing(TransportOrder::getDeadline).thenComparing(ordersByAge());
  }

  /**
   * A comparator for sorting transport orders by their age, with the oldest ones coming first.
   *
   * @return A comparator for sorting transport orders by their age.
   */
  public static Comparator<TransportOrder> ordersByAge() {
    return Comparator.comparing(TransportOrder::getCreationTime).thenComparing(objectsByName());
  }

  /**
   * A comparator for sorting peripheral jobs by their age, with the oldest ones coming first.
   *
   * @return A comparator for sorting peripheral jobs by their age.
   */
  public static Comparator<PeripheralJob> jobsByAge() {
    return Comparator.comparing(PeripheralJob::getCreationTime).thenComparing(objectsByName());
  }

  /**
   * A comparator for ordering ModelLayoutElements by the names of their
   * visualized objects.
   *
   * @return A comparator for ordering ModelLayoutElements by the names of their
   * visualized objects.
   */
  @Deprecated
  @ScheduledApiChange(details = "Will be removed.", when = "6.0")
  public static Comparator<org.opentcs.data.model.visualization.ModelLayoutElement>
      modelLayoutElementsByName() {
    return (org.opentcs.data.model.visualization.ModelLayoutElement o1,
        org.opentcs.data.model.visualization.ModelLayoutElement o2)
        -> o1.getVisualizedObject().getName().compareTo(
            o2.getVisualizedObject().getName());
  }
}
