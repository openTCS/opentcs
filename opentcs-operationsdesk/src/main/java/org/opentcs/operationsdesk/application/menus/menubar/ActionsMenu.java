/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.application.menus.menubar;

import static java.util.Objects.requireNonNull;
import static org.opentcs.operationsdesk.event.KernelStateChangeEvent.State.LOGGED_IN;

import jakarta.inject.Inject;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.jhotdraw.draw.Figure;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.guing.common.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.operationsdesk.application.action.ViewActionMap;
import org.opentcs.operationsdesk.application.action.actions.CreatePeripheralJobAction;
import org.opentcs.operationsdesk.application.action.actions.CreateTransportOrderAction;
import org.opentcs.operationsdesk.application.action.actions.FindVehicleAction;
import org.opentcs.operationsdesk.application.menus.MenuFactory;
import org.opentcs.operationsdesk.components.drawing.figures.VehicleFigure;
import org.opentcs.operationsdesk.event.KernelStateChangeEvent;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import org.opentcs.operationsdesk.util.OperationsDeskConfiguration;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;

/**
 * The application's menu for run-time actions.
 */
public class ActionsMenu
    extends
      JMenu
    implements
      EventHandler {

  /**
   * A menu item for creating new transport orders.
   */
  private final JMenuItem menuItemCreateTransportOrder;
  /**
   * A menu item for creating new peripheral jobs.
   */
  private final JMenuItem menuItemCreatePeripheralJob;
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
   * Creates a new instance.
   *
   * @param actionMap The application's action map.
   * @param drawingEditor The application's drawing editor.
   * @param menuFactory A factory for menu items.
   * @param appConfig The application's configuration.
   */
  @Inject
  @SuppressWarnings("this-escape")
  public ActionsMenu(
      ViewActionMap actionMap,
      OpenTCSDrawingEditor drawingEditor,
      MenuFactory menuFactory,
      OperationsDeskConfiguration appConfig,
      @ApplicationEventBus
      EventSource eventSource
  ) {
    requireNonNull(actionMap, "actionMap");
    requireNonNull(drawingEditor, "drawingEditor");
    requireNonNull(menuFactory, "menuFactory");
    requireNonNull(appConfig, "appConfig");
    requireNonNull(eventSource, "eventSource");

    final ResourceBundleUtil labels
        = ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.MENU_PATH);

    this.setText(labels.getString("actionsMenu.text"));
    this.setToolTipText(labels.getString("actionsMenu.tooltipText"));
    this.setMnemonic('A');

    // Menu item Actions -> Create Transport Order
    menuItemCreateTransportOrder = new JMenuItem(actionMap.get(CreateTransportOrderAction.ID));
    menuItemCreateTransportOrder.setEnabled(false);
    add(menuItemCreateTransportOrder);
    //Menu item Actions ->  Create Peripheral Job.
    menuItemCreatePeripheralJob = new JMenuItem(actionMap.get(CreatePeripheralJobAction.ID));
    menuItemCreatePeripheralJob.setEnabled(false);
    add(menuItemCreatePeripheralJob);
    addSeparator();

    // Menu item Actions -> Find Vehicle
    menuItemFindVehicle = new JMenuItem(actionMap.get(FindVehicleAction.ID));
    menuItemFindVehicle.setEnabled(false);
    add(menuItemFindVehicle);

    // Menu item Actions -> Ignore precise position
    cbiIgnorePrecisePosition = new JCheckBoxMenuItem(
        labels.getString("actionsMenu.menuItem_ignorePrecisePosition.text")
    );

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
    cbiIgnoreOrientationAngle = new JCheckBoxMenuItem(
        labels.getString("actionsMenu.menuItem_ignorePreciseOrientation.text")
    );

    add(cbiIgnoreOrientationAngle);
    cbiIgnoreOrientationAngle.setSelected(appConfig.ignoreVehicleOrientationAngle());
    cbiIgnoreOrientationAngle.addActionListener((ActionEvent e) -> {
      for (Figure figure : drawingEditor.getDrawing().getChildren()) {
        if (figure instanceof VehicleFigure) {
          ((VehicleFigure) figure).setIgnoreOrientationAngle(
              cbiIgnoreOrientationAngle.isSelected()
          );
        }
      }
    });

    eventSource.subscribe(this);
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof KernelStateChangeEvent kernelStateChangeEvent) {
      handleKernelStateChangeEvent(kernelStateChangeEvent);
    }
  }

  private void handleKernelStateChangeEvent(KernelStateChangeEvent event) {
    switch (event.getNewState()) {
      case LOGGED_IN:
        menuItemCreateTransportOrder.setEnabled(true);
        menuItemCreatePeripheralJob.setEnabled(true);
        menuItemFindVehicle.setEnabled(true);
        break;
      case DISCONNECTED:
        menuItemCreateTransportOrder.setEnabled(false);
        menuItemCreatePeripheralJob.setEnabled(false);
        menuItemFindVehicle.setEnabled(false);
        break;
      default:
        // Do nothing.
    }
  }
}
