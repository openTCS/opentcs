/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.menus.menubar;

import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.application.action.ViewActionMap;
import org.opentcs.guing.application.action.actions.CreateTransportOrderAction;
import org.opentcs.guing.application.action.course.DispatchVehicleAction;
import org.opentcs.guing.application.action.view.FindVehicleAction;
import org.opentcs.guing.application.menus.MenuFactory;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.components.drawing.figures.VehicleFigure;
import org.opentcs.guing.util.PlantOverviewApplicationConfiguration;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * The application's menu for run-time actions.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ActionsMenu
    extends JMenu {

  /**
   * A menu item for creating new transport orders.
   */
  private final JMenuItem menuItemCreateTransportOrder;
  /**
   * A menu item for finding a vehicle in the driving course.
   */
  private final JMenuItem menuItemFindVehicle;
  /**
   * A check box for ignoring the vehicles' precise positions.
   */
  private final JCheckBoxMenuItem cbiIgnorePrecisePosition;
  /**
   * A check box for ignoring the vehicles' orientation angles.
   */
  private final JCheckBoxMenuItem cbiIgnoreOrientationAngle;
  /**
   * A menu item for assuming the model coordinates from the layout coordinates.
   */
  private final JMenuItem cbiAlignLayoutWithModel;
  /**
   * A menu item for assuming the layout coordinates from the model coordinates.
   */
  private final JMenuItem cbiAlignModelWithLayout;
  /**
   * A menu item for triggering a dispatcher run.
   */
  private final JMenuItem menuItemDispatch;

  /**
   * Creates a new instance.
   *
   * @param actionMap The application's action map.
   * @param drawingEditor The application's drawing editor.
   * @param menuFactory A factory for menu items.
   * @param appConfig The application's configuration.
   */
  @Inject
  public ActionsMenu(ViewActionMap actionMap,
                     OpenTCSDrawingEditor drawingEditor,
                     MenuFactory menuFactory,
                     PlantOverviewApplicationConfiguration appConfig) {
    requireNonNull(actionMap, "actionMap");
    requireNonNull(drawingEditor, "drawingEditor");
    requireNonNull(menuFactory, "menuFactory");
    requireNonNull(appConfig, "appConfig");

    final ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

    // Menu item Actions -> Create Transport Order
    menuItemCreateTransportOrder = new JMenuItem(actionMap.get(CreateTransportOrderAction.ID));
    labels.configureMenu(menuItemCreateTransportOrder, CreateTransportOrderAction.ID);
    add(menuItemCreateTransportOrder);
    addSeparator();

    // Menu item Actions -> Find Vehicle
    menuItemFindVehicle = new JMenuItem(actionMap.get(FindVehicleAction.ID));
    labels.configureMenu(menuItemFindVehicle, FindVehicleAction.ID);
    add(menuItemFindVehicle);

    // Menu item Actions -> Ignore precise position
    cbiIgnorePrecisePosition = new JCheckBoxMenuItem("actions.ignorePrecisePosition");
    labels.configureMenu(cbiIgnorePrecisePosition, "actions.ignorePrecisePosition");
    add(cbiIgnorePrecisePosition);
    cbiIgnorePrecisePosition.setSelected(appConfig.ignoreVehiclePrecisePosition());
    cbiIgnorePrecisePosition.addActionListener((ActionEvent e) -> {
      for (Figure figure : drawingEditor.getDrawing().getChildren()) {
        if (figure instanceof VehicleFigure) {
          ((VehicleFigure) figure).setIgnorePrecisePosition(cbiIgnorePrecisePosition.isSelected());
        }
      }
    });

    // Menu item Actions -> Ignore orientation angle
    cbiIgnoreOrientationAngle = new JCheckBoxMenuItem(actionMap.get("actions.ignoreOrientationAngle"));
    labels.configureMenu(cbiIgnoreOrientationAngle, "actions.ignoreOrientationAngle");
    add(cbiIgnoreOrientationAngle);
    cbiIgnoreOrientationAngle.setSelected(appConfig.ignoreVehicleOrientationAngle());
    cbiIgnoreOrientationAngle.addActionListener((ActionEvent e) -> {
      for (Figure figure : drawingEditor.getDrawing().getChildren()) {
        if (figure instanceof VehicleFigure) {
          ((VehicleFigure) figure).setIgnoreOrientationAngle(cbiIgnoreOrientationAngle.isSelected());
        }
      }
    });

    // Menu item Actions -> Copy model to layout
    cbiAlignModelWithLayout = menuFactory.createModelToLayoutMenuItem(true);
    add(cbiAlignModelWithLayout);

    // Menu item Actions -> Copy layout to model
    cbiAlignLayoutWithModel = menuFactory.createLayoutToModelMenuItem(true);
    add(cbiAlignLayoutWithModel);

    addSeparator();

    menuItemDispatch = new JMenuItem(actionMap.get(DispatchVehicleAction.ID));
    labels.configureMenu(menuItemDispatch, DispatchVehicleAction.ID);
    add(menuItemDispatch);
  }

  /**
   * Updates the menu's items for the given mode of operation.
   *
   * @param mode The new mode of operation.
   */
  public void setOperationMode(OperationMode mode) {
    requireNonNull(mode, "mode");

    menuItemCreateTransportOrder.setEnabled(mode == OperationMode.OPERATING);
    menuItemFindVehicle.setEnabled(mode == OperationMode.OPERATING);
    cbiIgnorePrecisePosition.setEnabled(mode == OperationMode.OPERATING);
    cbiIgnoreOrientationAngle.setEnabled(mode == OperationMode.OPERATING);
    cbiAlignLayoutWithModel.setEnabled(mode == OperationMode.MODELLING);
    cbiAlignModelWithLayout.setEnabled(mode == OperationMode.MODELLING);
    menuItemDispatch.setEnabled(mode == OperationMode.OPERATING);
  }
}
