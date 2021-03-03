/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.ApplicationFrame;
import org.opentcs.guing.components.dialogs.ClosableDialog;
import org.opentcs.guing.components.dialogs.FindVehiclePanel;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to find a vehicle on the drawing.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class FindVehicleAction
    extends AbstractAction {

  /**
   * This action's ID.
   */
  public final static String ID = "actions.findVehicle";
  /**
   * Provides the current system model.
   */
  private final ModelManager modelManager;
  /**
   * The drawing editor.
   */
  private final OpenTCSDrawingEditor drawingEditor;
  /**
   * The parent component for dialogs shown by this action.
   */
  private final Component dialogParent;

  /**
   * Creates a new instance.
   *
   * @param modelManager Provides the current system model.
   * @param drawingEditor The drawing editor.
   * @param dialogParent The parent component for dialogs shown by this action.
   */
  @Inject
  public FindVehicleAction(ModelManager modelManager,
                           OpenTCSDrawingEditor drawingEditor,
                           @ApplicationFrame Component dialogParent) {
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");

    ResourceBundleUtil.getBundle().configureAction(this, ID);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    findVehicle();
  }

  private void findVehicle() {
    List<VehicleModel> vehicles
        = new ArrayList<>(modelManager.getModel().getVehicleModels());
    if (vehicles.isEmpty()) {
      return;
    }

    Collections.sort(vehicles);
    FindVehiclePanel content
        = new FindVehiclePanel(vehicles, drawingEditor.getActiveView());
    String title = ResourceBundleUtil.getBundle().getString("findVehiclePanel.title");
    ClosableDialog dialog = new ClosableDialog(dialogParent, true, content, title);
    dialog.setLocationRelativeTo(dialogParent);
    dialog.setVisible(true);
  }

}
