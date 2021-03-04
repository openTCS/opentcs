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
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Some commonly used comparator implementations.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class Comparators {

  /**
   * A <code>Comparator</code> for ordering <code>TCSObject</code>s ascendingly
   * by their IDs.
   *
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  private static final Comparator<TCSObject<?>> OBJECTS_BY_ID
      = (TCSObject<?> o1, TCSObject<?> o2) -> o1.getId() - o2.getId();

  /**
   * A <code>Comparator</code> for ordering <code>TCSObject</code>s ascendingly
   * by their names.
   */
  private static final Comparator<TCSObject<?>> OBJECTS_BY_NAME
      = (TCSObject<?> o1, TCSObject<?> o2) -> o1.getName().compareTo(o2.getName());
  /**
   * A <code>Comparator</code> for ordering <code>TCSObjectReference</code>s ascendingly
   * by their names.
   */
  private static final Comparator<TCSObjectReference<?>> REFERENCES_BY_NAME
      = (TCSObjectReference<?> o1, TCSObjectReference<?> o2) -> o1.getName().compareTo(o2.getName());
  /**
   * A comparator for sorting transport orders by their age, with the oldest ones coming first.
   */
  private static final Comparator<TransportOrder> ORDERS_BY_AGE
      = (TransportOrder o1, TransportOrder o2) -> {
        if (o1.getCreationTime() < o2.getCreationTime()) {
          return -1;
        }
        else if (o1.getCreationTime() > o2.getCreationTime()) {
          return 1;
        }
        else {
          return OBJECTS_BY_NAME.compare(o1, o2);
        }
      };
  /**
   * A comparator for sorting transport orders by their deadline, with those with the earliest
   * deadline coming first.
   * If two orders have exactly the same deadline, they are sorted by their (unique) creation
   * times, with the older one coming first.
   */
  private static final Comparator<TransportOrder> ORDERS_BY_DEADLINE
      = (TransportOrder o1, TransportOrder o2) -> {
        if (o1.getDeadline() < o2.getDeadline()) {
          return -1;
        }
        else if (o1.getDeadline() > o2.getDeadline()) {
          return 1;
        }
        else {
          return ORDERS_BY_AGE.compare(o1, o2);
        }
      };
  /**
   * Compares ModelLayoutElements by the names of their visualized objects.
   */
  private static final Comparator<ModelLayoutElement> LAYOUT_ELEMS_BY_NAME
      = (ModelLayoutElement o1, ModelLayoutElement o2)
      -> o1.getVisualizedObject().getName().compareTo(
          o2.getVisualizedObject().getName());
  /**
   * Compares TravelCosts by their costs.
   *
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  private static final Comparator<org.opentcs.access.TravelCosts> TRAVEL_COSTS_BY_COSTS
      = (org.opentcs.access.TravelCosts t1, org.opentcs.access.TravelCosts t2) -> {
        if (t1.getCosts() < t2.getCosts()) {
          return -1;
        }
        else if (t1.getCosts() > t2.getCosts()) {
          return 1;
        }
        else {
          return 0;
        }
      };

  /**
   * Prevents undesired instantiation.
   */
  private Comparators() {
  }

  /**
   * Returns a comparator for ordering <code>TCSObject</code>s ascendingly
   * by their IDs.
   *
   * @return A comparator for ordering <code>TCSObject</code>s ascendingly
   * by their IDs.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public static Comparator<TCSObject<?>> objectsById() {
    return OBJECTS_BY_ID;
  }

  /**
   * Returns a comparator for ordering <code>TCSObject</code>s ascendingly
   * by their names.
   *
   * @return A comparator for ordering <code>TCSObject</code>s ascendingly
   * by their names.
   */
  public static Comparator<TCSObject<?>> objectsByName() {
    return OBJECTS_BY_NAME;
  }

  /**
   * Returns a comparator for ordering <code>TCSObjectReference</code>s ascendingly
   * by their names.
   *
   * @return A comparator for ordering <code>TCSObjectReference</code>s ascendingly
   * by their names.
   */
  public static Comparator<TCSObjectReference<?>> referencesByName() {
    return REFERENCES_BY_NAME;
  }

  /**
   * A comparator for sorting transport orders by their priority, with those
   * with the highest priority coming first.
   * Transport orders are sorted by their deadlines first; if two orders have
   * exactly the same deadline, they are sorted by their (unique) creation
   * times, with the older one coming first.
   *
   * @return A comparator for sorting transport orders by their priority.
   */
  public static Comparator<TransportOrder> ordersByDeadline() {
    return ORDERS_BY_DEADLINE;
  }

  /**
   * A comparator for sorting transport orders by their age, with the oldest
   * ones coming first.
   *
   * @return A comparator for sorting transport orders by their age.
   */
  public static Comparator<TransportOrder> ordersByAge() {
    return ORDERS_BY_AGE;
  }

  /**
   * A comparator for ordering ModelLayoutElements by the names of their
   * visualized objects.
   *
   * @return A comparator for ordering ModelLayoutElements by the names of their
   * visualized objects.
   */
  public static Comparator<ModelLayoutElement> modelLayoutElementsByName() {
    return LAYOUT_ELEMS_BY_NAME;
  }

  /**
   * A comparator for ordering {@link org.opentcs.access.TravelCosts} by their respective costs.
   *
   * @return The comparator.
   * @deprecated Providing travel costs to external clients will not be part of the standard kernel
   * API any more.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public static Comparator<org.opentcs.access.TravelCosts> travelCostsByCosts() {
    return TRAVEL_COSTS_BY_COSTS;
  }
}
