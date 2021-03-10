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
import static javax.swing.Action.LARGE_ICON_KEY;
import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.ImageIcon;
import org.opentcs.customizations.plantoverview.ApplicationFrame;
import org.opentcs.guing.components.dialogs.ClosableDialog;
import org.opentcs.guing.components.dialogs.FindVehiclePanel;
import org.opentcs.guing.components.dialogs.FindVehiclePanelFactory;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.Comparators;
import org.opentcs.guing.util.I18nPlantOverview;
import static org.opentcs.guing.util.I18nPlantOverview.MENU_PATH;
import org.opentcs.guing.util.ImageDirectory;
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

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);
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
   * The panel factory.
   */
  private final FindVehiclePanelFactory panelFactory;

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
                           @ApplicationFrame Component dialogParent,
                           FindVehiclePanelFactory panelFactory) {
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");
    this.panelFactory = requireNonNull(panelFactory, "panelFactory");

    putValue(NAME, BUNDLE.getString("findVehicleAction.name"));
    putValue(MNEMONIC_KEY, Integer.valueOf('F'));

    ImageIcon icon = ImageDirectory.getImageIcon("/toolbar/find-vehicle.22.png");
    putValue(SMALL_ICON, icon);
    putValue(LARGE_ICON_KEY, icon);
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

    Collections.sort(vehicles, Comparators.modelComponentsByName());
    FindVehiclePanel content = panelFactory.createFindVehiclesPanel(vehicles,
                                                                    drawingEditor.getActiveView());
    String title = ResourceBundleUtil.getBundle(I18nPlantOverview.FINDVEHICLE_PATH)
        .getString("findVehicleAction.dialog_findVehicle.title");
    ClosableDialog dialog = new ClosableDialog(dialogParent, true, content, title);
    dialog.setLocationRelativeTo(dialogParent);
    dialog.setVisible(true);
  }

}
