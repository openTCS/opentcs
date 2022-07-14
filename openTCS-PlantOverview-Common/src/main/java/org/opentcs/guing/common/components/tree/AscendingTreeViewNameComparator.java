/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.tree;

import java.util.Comparator;

/**
 * Compares two elements of a tree view for sorting.
 * Sorts based on their name in descending order.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class AscendingTreeViewNameComparator
    implements Comparator<Object> {

  /**
   * Creates a new instance.
   */
  public AscendingTreeViewNameComparator() {
  }

  @Override
  public int compare(Object o1, Object o2) {
    String s1 = o1.toString();
    String s2 = o2.toString();
    s1 = s1.toLowerCase();
    s2 = s2.toLowerCase();

    return s1.compareTo(s2);
  }
}
