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

import com.google.inject.assistedinject.Assisted;
import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.application.menus.MenuFactory;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.IconToolkit;

/**
 * Ein Fahrzeug-Objekt in der Baumansicht.
 * <p>
 * <b>Entwurfsmuster:</b> Befehl.
 * VehicleUserObject ist ein konkreter Befehl.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class VehicleUserObject
    extends AbstractUserObject {

  /**
   * Stores the application's current state.
   */
  private final ApplicationState appState;
  /**
   * A factory for popup menus.
   */
  private final MenuFactory menuFactory;
  /**
   * All selected vehicles.
   */
  protected Set<VehicleModel> selectedVehicles;

  /**
   * Creates a new instance.
   *
   * @param model The corresponding vehicle object.
   * @param appState Stores the application's current state.
   * @param view The application's main view.
   * @param editor The drawing editor.
   * @param modelManager Provides the current system model.
   * @param menuFactory A factory for popup menus.
   */
  @Inject
  public VehicleUserObject(@Assisted VehicleModel model,
                           ApplicationState appState,
                           OpenTCSView view,
                           OpenTCSDrawingEditor editor,
                           ModelManager modelManager,
                           MenuFactory menuFactory) {
    super(model, view, editor, modelManager);
    this.appState = requireNonNull(appState, "appState");
    this.menuFactory = requireNonNull(menuFactory, "menuFactory");
  }

  @Override
  public VehicleModel getModelComponent() {
    return (VehicleModel) super.getModelComponent();
  }

  @Override  // AbstractUserObject
  public void doubleClicked() {
    getView().figureSelected(getModelComponent());
  }

  @Override // UserObject
  public void rightClicked(JComponent component, int x, int y) {
    selectedVehicles = getSelectedVehicles(((JTree) component));
    super.rightClicked(component, x, y);
  }

  @Override  // AbstractUserObject
  public JPopupMenu getPopupMenu() {
    if (appState.hasOperationMode(OperationMode.OPERATING)) {
      return menuFactory.createVehiclePopupMenu(selectedVehicles);
    }

    return null;
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
