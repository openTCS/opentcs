/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.application.action.actions;

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
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.components.dialogs.ClosableDialog;
import org.opentcs.guing.common.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.util.Comparators;
import org.opentcs.guing.common.util.ImageDirectory;
import org.opentcs.operationsdesk.components.dialogs.FindVehiclePanel;
import org.opentcs.operationsdesk.components.dialogs.FindVehiclePanelFactory;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import static org.opentcs.operationsdesk.util.I18nPlantOverviewOperating.MENU_PATH;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

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
   * @param panelFactory The panel factory.
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
    String title = ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.FINDVEHICLE_PATH)
        .getString("findVehicleAction.dialog_findVehicle.title");
    ClosableDialog dialog = new ClosableDialog(dialogParent, true, content, title);
    dialog.setLocationRelativeTo(dialogParent);
    dialog.setVisible(true);
  }

}
