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
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.application.menus.MenuFactory;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.model.elements.VehicleModel;
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

  @Override  // AbstractUserObject
  public JPopupMenu getPopupMenu() {
    if (appState.hasOperationMode(OperationMode.OPERATING)) {
      return menuFactory.createVehiclePopupMenu(getModelComponent());
    }

    return null;
  }

  @Override  // AbstractUserObject
  public ImageIcon getIcon() {
    return IconToolkit.instance().createImageIcon("tree/vehicle.18x18.png");
  }
}
