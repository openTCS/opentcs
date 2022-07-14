/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.tree.elements;

import com.google.inject.assistedinject.Assisted;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.application.GuiManager;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.util.IconToolkit;

/**
 * Represents a vehicle object in the TreeView.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class VehicleUserObject
    extends AbstractUserObject {

  /**
   * All selected vehicles.
   */
  protected Set<VehicleModel> selectedVehicles;

  /**
   * Creates a new instance.
   *
   * @param model The corresponding vehicle object.
   * @param guiManager The gui manager.
   * @param modelManager Provides the current system model.
   */
  @Inject
  public VehicleUserObject(@Assisted VehicleModel model,
                           GuiManager guiManager,
                           ModelManager modelManager) {
    super(model, guiManager, modelManager);
  }

  @Override
  public VehicleModel getModelComponent() {
    return (VehicleModel) super.getModelComponent();
  }

  @Override  // AbstractUserObject
  public void doubleClicked() {
    getGuiManager().figureSelected(getModelComponent());
  }

  @Override // UserObject
  public void rightClicked(JComponent component, int x, int y) {
    selectedVehicles = getSelectedVehicles(((JTree) component));
    super.rightClicked(component, x, y);
  }

  @Override  // AbstractUserObject
  public ImageIcon getIcon() {
    return IconToolkit.instance().createImageIcon("tree/vehicle.18x18.png");
  }

  /**
   * Returns the selected vehicle models in the tree.
   *
   * @param objectTree The tree to find the selected items.
   * @return All selected vehicle models.
   */
  private Set<VehicleModel> getSelectedVehicles(JTree objectTree) {
    Set<VehicleModel> objects = new HashSet<>();
    TreePath[] selectionPaths = objectTree.getSelectionPaths();

    if (selectionPaths != null) {
      for (TreePath path : selectionPaths) {
        if (path != null) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          //vehicles can only be selected with other vehicles
          if (node.getUserObject() instanceof VehicleUserObject) {
            objects.add((VehicleModel) ((UserObject) node.getUserObject()).getModelComponent());
          }
        }
      }
    }

    return objects;
  }
}
