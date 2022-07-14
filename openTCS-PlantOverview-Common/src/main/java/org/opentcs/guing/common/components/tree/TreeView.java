/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.tree;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import org.opentcs.guing.common.components.tree.elements.UserObject;

/**
 * A TreeView manages a model which has a set of TreeNode objects.
 * Each TreeNode has a UserObject. Each UserObject wraps a real object 
 * (e.g. Article, Figure, ...). It knows which methods to call to when an object is selected 
 * in the tree or is deleted or is double-clicked.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see UserObject
 */
public interface TreeView {

  /**
   * Adds an item to the tree.
   * 
   * @param parent A real object that is seached in the tree.
   * @param item Is a UserObject.
   */
  void addItem(Object parent, UserObject item);

  /**
   * Sorts the children of the given node.
   *
   * @param treeNode The node whose children shall be sorted.
   */
  void sortItems(TreeNode treeNode);

  /**
   * Returns the <code>JTree</code> that actually holds the objects.
   *
   * @return The tree.
   */
  JTree getTree();

  /**
   * Adds the given listener to the <code>JTree</code>.
   *
   * @param mouseListener The listener.
   */
  void addMouseListener(MouseListener mouseListener);

  /**
   * Adds the given motion listener to the <code>JTree</code>.
   *
   * @param mouseMotionListener The motion listener.
   */
  void addMouseMotionListener(MouseMotionListener mouseMotionListener);

  /**
   * Updates the text at the top of the <code>JTree</code>.
   *
   * @param text The new text.
   */
  void updateText(String text);

  /**
   * Returns whether the tree has buffered objects.
   *
   * @return <code>true</code> if it has some.
   */
  boolean hasBufferedObjects();

  /**
   * Returns the dragged user object.
   *
   * @param e The event where the mouse click happened.
   * @return The user object that was dragged.
   */
  UserObject getDraggedUserObject(MouseEvent e);

  /**
   * Sets the cursor of the <code>JTree</code>.
   *
   * @param cursor The new cursor.
   */
  void setCursor(Cursor cursor);

  /**
   * Removes an item from the tree.
   *
   * @param item A real object.
   */
  void removeItem(Object item);

  /**
   * Removes all child elements from an item.
   *
   * @param item The item to remove child elements from.
   */
  void removeChildren(Object item);

  /**
   * Selects an item in the tree.
   *
   * @param item The item to select.
   */
  void selectItem(Object item);

  /**
   * Selects a set of items in the tree.
   *
   * @param items The set of items to select.
   */
  void selectItems(Set<?> items);

  /**
   * Notifies the tree that a property of the specified item has changed.
   *
   * @param item
   */
  void itemChanged(Object item);

  /**
   * Return the selected item.
   *
   * @return the selected item.
   */
  UserObject getSelectedItem();

  /**
   * Return the selected items.
   *
   * @return the selected items.
   */
  Set<UserObject> getSelectedItems();

  /**
   * Sorts the root element of the tree.
   */
  void sortRoot();

  /**
   * Sorts all children.
   */
  void sortChildren();
}
