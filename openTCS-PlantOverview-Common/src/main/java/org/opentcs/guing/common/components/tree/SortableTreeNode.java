/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.tree;

import java.util.Collections;
import java.util.Comparator;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import org.slf4j.LoggerFactory;

/**
 * A tree node that can be sorted.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class SortableTreeNode
    extends DefaultMutableTreeNode {

  /**
   * True if the child elements of this node can be sorted.
   */
  private boolean fSorting;

  /**
   * Creates a new instance.
   *
   * @param userObject The UserObject.
   */
  public SortableTreeNode(Object userObject) {
    super(userObject);
    setSorting(true);
  }

  /**
   * Sets whether or not the child elements of this node can be sorted.
   *
   * @param sorting True if the child elements of this node can be sorted.
   */
  public final void setSorting(boolean sorting) {
    fSorting = sorting;
  }

  /**
   * Returns true if the child elements of this node can be sorted.
   * 
   * @return True if the child elements of this node can be sorted.
   */
  public boolean isSorting() {
    return fSorting;
  }

  /**
   * Sort the child elements.
   *
   * @param comparator The comparator to be used for sorting.
   */
  @SuppressWarnings("unchecked")
  public void sort(Comparator<Object> comparator) {
    Collections.sort(children, comparator);
  }

  @Override
  public TreeNode getChildAt(int index) {
    try {
      return super.getChildAt(index);
    }
    catch (ArrayIndexOutOfBoundsException e) {
      // XXX remove if never observed
      LoggerFactory.getLogger(SortableTreeNode.class)
          .error("Exception while calling SortableTreeNode.getChildAt()", e);
      return null;
    }
  }
}
