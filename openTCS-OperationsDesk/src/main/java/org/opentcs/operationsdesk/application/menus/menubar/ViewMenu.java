/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.application.menus.menubar;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opentcs.guing.common.application.OperationMode;
import org.opentcs.guing.common.application.menus.menubar.ViewPluginPanelsMenu;
import org.opentcs.operationsdesk.application.action.ViewActionMap;
import org.opentcs.operationsdesk.application.action.view.AddDrawingViewAction;
import org.opentcs.operationsdesk.application.action.view.AddTransportOrderSequenceViewAction;
import org.opentcs.operationsdesk.application.action.view.AddTransportOrderViewAction;
import org.opentcs.operationsdesk.application.action.view.RestoreDockingLayoutAction;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * The application's menu for view-related operations.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ViewMenu
    extends JMenu {

  /**
   * A menu item for adding a drawing view.
   */
  private final JMenuItem menuAddDrawingView;
  /**
   * A menu item for adding a transport order view.
   */
  private final JMenuItem menuTransportOrderView;
  /**
   * A menu item for adding an order sequence view.
   */
  private final JMenuItem menuOrderSequenceView;
  /**
   * A menu for showing/hiding plugin panels.
   */
  private final ViewPluginPanelsMenu menuPluginPanels;
  /**
   * A menu item for restoring the default GUI layout.
   */
  private final JMenuItem menuItemRestoreDockingLayout;

  /**
   * Creates a new instance.
   *
   * @param actionMap The application's action map.
   * @param menuPluginPanels A menu for showing/hiding plugin panels.
   */
  @Inject
  public ViewMenu(ViewActionMap actionMap,
                  ViewPluginPanelsMenu menuPluginPanels) {
    requireNonNull(actionMap, "actionMap");
    requireNonNull(menuPluginPanels, "menuPluginPanels");

    final ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.MENU_PATH);

    this.setText(labels.getString("viewMenu.text"));
    this.setToolTipText(labels.getString("viewMenu.tooltipText"));
    this.setMnemonic('V');

    // Menu item View -> Add course view
    menuAddDrawingView = new JMenuItem(actionMap.get(AddDrawingViewAction.ID));
    add(menuAddDrawingView);

    // Menu item View -> Add transport order view
    menuTransportOrderView = new JMenuItem(actionMap.get(AddTransportOrderViewAction.ID));
    add(menuTransportOrderView);

    // Menu item View -> Add transport order sequence view
    menuOrderSequenceView = new JMenuItem(actionMap.get(AddTransportOrderSequenceViewAction.ID));
    add(menuOrderSequenceView);

    addSeparator();

    // Menu item View -> Plugins
    this.menuPluginPanels = menuPluginPanels;
    menuPluginPanels.setOperationMode(OperationMode.OPERATING);
    add(menuPluginPanels);

    // Menu item View -> Restore docking layout
    menuItemRestoreDockingLayout = new JMenuItem(actionMap.get(RestoreDockingLayoutAction.ID));
    menuItemRestoreDockingLayout.setText(labels.getString("viewMenu.menuItem_restoreWindowArrangement.text"));
    add(menuItemRestoreDockingLayout);
  }
}
