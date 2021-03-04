/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree.elements;

import java.util.HashSet;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.util.IconToolkit;

/**
 * Die Repräsentation eines Figure-Objekts in der Baumansicht. Korrekter wäre
 * es, wenn FigureUserObject nicht FigureUserObject hieße, sondern
 * FigureComponentUserObject, denn es fungiert als Stellvertreter für ein
 * FigureComponent-Objekt und nicht für ein Figure-Objekt.
 * <p>
 * <b>Entwurfsmuster:</b> Befehl. FigureUserObject ist ein konkreter Befehl.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see FigureComponent
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
   * @param view The openTCS view
   * @param editor The drawing editor
   * @param modelManager The model manager
   */
  public FigureUserObject(ModelComponent modelComponent,
                          OpenTCSView view,
                          OpenTCSDrawingEditor editor,
                          ModelManager modelManager) {
    super(modelComponent, view, editor, modelManager);
  }

  @Override // AbstractUserObject
  public String toString() {
    return getModelComponent().getDescription() + " "
        + getModelComponent().getName();
  }

  @Override // AbstractUserObject
  public void doubleClicked() {
    getView().figureSelected(getModelComponent());
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
          objects.add((UserObject) node.getUserObject());
        }
      }
    }

    return objects;
  }
}
