/*
 * openTCS copyright information:
 * Copyright (c) 2005 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import java.util.Comparator;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.order.TransportOrder;

/**
 * Some commonly used comparator implementations.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class Comparators {

  /**
   * A <code>Comparator</code> for ordering <code>TCSObject</code>s ascendingly
   * by their IDs.
   */
  private static final Comparator<TCSObject<?>> idComparator
      = (TCSObject<?> o1, TCSObject<?> o2) -> o1.getId() - o2.getId();

  /**
   * A <code>Comparator</code> for ordering <code>TCSObject</code>s ascendingly
   * by their names.
   */
  private static final Comparator<TCSObject<?>> nameComparator
      = (TCSObject<?> o1, TCSObject<?> o2) -> o1.getName().compareTo(o2.getName());
  /**
   * A comparator for sorting transport orders by their priority, with those
   * with the highest priority coming first.
   * Transport orders are sorted by their deadlines first; if two orders have
   * exactly the same deadline, they are sorted by their (unique) creation
   * times, with the older one coming first.
   */
  private static final Comparator<TransportOrder> priorityComparator
      = (TransportOrder o1, TransportOrder o2) -> {
        int result;
        long difference = o1.getDeadline() - o2.getDeadline();
        if (difference < 0) {
          result = -1;
        }
        else if (difference > 0) {
          result = 1;
        }
        else {
          long ageDifference = o1.getCreationTime() - o2.getCreationTime();
          if (ageDifference < 0) {
            result = -1;
          }
          else if (ageDifference > 0) {
            result = 1;
          }
          else {
            result = 0;
          }
        }
        return result;
      };

  /**
   * A comparator for sorting transport orders by their age, with the oldest
   * ones coming first.
   */
  private static final Comparator<TransportOrder> ageComparator
      = (TransportOrder o1, TransportOrder o2) -> {
        int result;
        long ageDifference = o1.getCreationTime() - o2.getCreationTime();
        if (ageDifference < 0) {
          result = -1;
        }
        else if (ageDifference > 0) {
          result = 1;
        }
        else {
          int idDifference = o1.getId() - o2.getId();
          if (idDifference < 0) {
            result = -1;
          }
          else if (idDifference > 0) {
            result = 1;
          }
          else {
            result = 0;
          }
        }
        return result;
      };

  /**
   * Compares ModelLayoutElements by the names of their visualized objects.
   */
  private static final Comparator<ModelLayoutElement> layoutElementsByName
      = (ModelLayoutElement o1, ModelLayoutElement o2)
      -> o1.getVisualizedObject().getName().compareTo(
          o2.getVisualizedObject().getName());

  /**
   * Prevents undesired instantiation.
   */
  private Comparators() {
    // Do nada.
  }

  /**
   * Returns a comparator for ordering <code>TCSObject</code>s ascendingly
   * by their IDs.
   *
   * @return A comparator for ordering <code>TCSObject</code>s ascendingly
   * by their IDs.
   */
  public static Comparator<TCSObject<?>> objectsById() {
    return idComparator;
  }

  /**
   * Returns a comparator for ordering <code>TCSObject</code>s ascendingly
   * by their names.
   *
   * @return A comparator for ordering <code>TCSObject</code>s ascendingly
   * by their names.
   */
  public static Comparator<TCSObject<?>> objectsByName() {
    return nameComparator;
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
  public static Comparator<TransportOrder> ordersByPriority() {
    return priorityComparator;
  }

  /**
   * A comparator for sorting transport orders by their age, with the oldest
   * ones coming first.
   *
   * @return A comparator for sorting transport orders by their age.
   */
  public static Comparator<TransportOrder> ordersByAge() {
    return ageComparator;
  }
  
  /**
   * A comparator for ordering ModelLayoutElements by the names of their
   * visualized objects.
   * 
   * @return A comparator for ordering ModelLayoutElements by the names of their
   * visualized objects.
   */
  public static Comparator<ModelLayoutElement> modelLayoutElementsByName() {
    return layoutElementsByName;
  }
}
