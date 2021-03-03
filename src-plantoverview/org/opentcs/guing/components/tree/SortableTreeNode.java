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

import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * Ein TreeNode, dessen Elemente sortiert werden können.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class SortableTreeNode
    extends DefaultMutableTreeNode {

  /**
   * True, wenn die Kindelemente dieses Knotens sortiert werden sollen.
   */
  private boolean fSorting;

  /**
   * Erzeugt ein neues Objekt von SortableTreeNode.
   *
   * @param userObject Das UserObject.
   */
  public SortableTreeNode(Object userObject) {
    super(userObject);
    setSorting(true);
  }

  /**
   *
   * @param sorting true, wenn die Kindelemente des Knotens sortiert werden
   * sollen.
   */
  public final void setSorting(boolean sorting) {
    fSorting = sorting;
  }

  /**
   *
   * @return true, wenn die Kindelemente des Knotens sortiert werden sollen.
   */
  public boolean isSorting() {
    return fSorting;
  }

  /**
   * Sortiert die Kindelemente. Um die Darstellung zu aktualisieren, ist ein
   * reload() im TreeModel nötig.
   *
   * @param comparator
   */
  public void sort(Comparator comparator) {
    Collections.sort(children, comparator);
  }
  
  @Override
  public TreeNode getChildAt(int index) {
    try {
      return super.getChildAt(index);
    }
    catch (ArrayIndexOutOfBoundsException e) {
      // XXX remove if never observed
      Logger.getLogger(SortableTreeNode.class.getName()).log(Level.SEVERE,
                                                             "Exception while calling SortableTreeNode.getChildAt()",
                                                             e);
      return null;
    }
  }
}
