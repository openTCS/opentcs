/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree;

import java.util.Comparator;

/**
 * Vergleicht zwei Elemente der Baumansicht zu Zwecken der Sortierung. Die
 * Sortierung erfolgt in absteigender Reihenfolge nach dem Namen.
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
