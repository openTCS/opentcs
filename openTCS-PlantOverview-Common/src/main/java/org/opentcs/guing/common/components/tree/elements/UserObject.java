/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.tree.elements;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A UserObject has the purpose of representing a model component in the TreeView.
 * It manages its model component and is responsible for executing user interactions e.g. selecting,
 * deleting, double clicking.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see ModelComponent
 */
public interface UserObject {

  /**
   * Returns the wrapped model component.
   *
   * @return the wrapped model component.
   */
  ModelComponent getModelComponent();

  /**
   * Return the popup menu for the model component.
   *
   * @return the popup menu for the model component.
   */
  JPopupMenu getPopupMenu();

  /**
   * Return the icon for this user object.
   *
   * @return the icon for this user object.
   */
  ImageIcon getIcon();

  /**
   * Is called when the object is selected in the tree view.
   */
  void selected();

  /**
   * Is called when this object is removed from the tree view.
   *
   * @return
   */
  boolean removed();

  /**
   * Is called when the object was right clicked in the tree view.
   *
   * @param component
   * @param x X position of the mouse click.
   * @param y Y position of the mouse click.
   */
  void rightClicked(JComponent component, int x, int y);

  /**
   * Is called when the object is double clicked in the tree view.
   */
  void doubleClicked();

  /**
   * Returns the parent component that contains this user object.
   * (Typically a <code>SimpleFolder</code>
   *
   * @return The parent <code>ModelComponent</code>.
   */
  ModelComponent getParent();

  /**
   * Sets the parent component.
   *
   * @param parent The parent.
   */
  void setParent(ModelComponent parent);
}
