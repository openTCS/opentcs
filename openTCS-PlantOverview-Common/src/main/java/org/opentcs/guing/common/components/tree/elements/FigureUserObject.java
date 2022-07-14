/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.tree.elements;

import java.util.HashSet;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.common.application.GuiManager;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.util.IconToolkit;

/**
 * Represents a Figure component in the TreeView.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class FigureUserObject
    extends AbstractUserObject {

  /**
   * All selected user objects.
   */
  protected Set<UserObject> userObjectItems;

  /**
   * Creates a new instance.
   *
   * @param modelComponent The corresponding data object
   * @param guiManager The gui manager.
   * @param modelManager The model manager
   */
  public FigureUserObject(ModelComponent modelComponent,
                          GuiManager guiManager,
                          ModelManager modelManager) {
    super(modelComponent, guiManager, modelManager);
  }

  @Override // AbstractUserObject
  public String toString() {
    return getModelComponent().getDescription() + " "
        + getModelComponent().getName();
  }

  @Override // AbstractUserObject
  public void doubleClicked() {
    getGuiManager().figureSelected(getModelComponent());
  }

  @Override // AbstractUserObject
  public ImageIcon getIcon() {
    return IconToolkit.instance().createImageIcon("tree/figure.18x18.png");
  }

  @Override // UserObject
  public void rightClicked(JComponent component, int x, int y) {
    userObjectItems = getSelectedUserObjects(((JTree) component));
    super.rightClicked(component, x, y);
  }

  /**
   * Returns the selected user objects in the tree.
   *
   * @param objectTree The tree to find the selected items.
   * @return All selected user objects.
   */
  private Set<UserObject> getSelectedUserObjects(JTree objectTree) {
    Set<UserObject> objects = new HashSet<>();
    TreePath[] selectionPaths = objectTree.getSelectionPaths();

    if (selectionPaths != null) {
      for (TreePath path : selectionPaths) {
        if (path != null) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          if (node.getUserObject() instanceof FigureUserObject) {
            objects.add((UserObject) node.getUserObject());
          }
        }
      }
    }

    return objects;
  }
}
