/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.application.menus.menubar;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import org.jhotdraw.app.action.window.ToggleVisibleAction;
import org.opentcs.guing.common.application.OperationMode;
import org.opentcs.guing.common.application.menus.menubar.ViewPluginPanelsMenu;
import org.opentcs.modeleditor.application.action.ToolBarManager;
import org.opentcs.modeleditor.application.action.ViewActionMap;
import org.opentcs.modeleditor.application.action.view.AddBitmapAction;
import org.opentcs.modeleditor.application.action.view.RestoreDockingLayoutAction;
import org.opentcs.modeleditor.util.I18nPlantOverviewModeling;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * The application's menu for view-related operations.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ViewMenu
    extends JMenu {

  /**
   * The toolbar manager.
   */
  private final ToolBarManager toolBarManager;
  /**
   * A menu item for setting a bitmap for the current drawing view.
   */
  private final JMenuItem menuAddBitmap;
  /**
   * A menu for manipulating the application's tool bars.
   */
  private final ViewToolBarsMenu menuViewToolBars;
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
   * @param toolBarManager The toolbar manager.
   * @param menuPluginPanels A menu for showing/hiding plugin panels.
   */
  @Inject
  public ViewMenu(ViewActionMap actionMap,
                  ToolBarManager toolBarManager,
                  ViewPluginPanelsMenu menuPluginPanels) {
    requireNonNull(actionMap, "actionMap");
    this.toolBarManager = requireNonNull(toolBarManager, "toolBarManager");
    requireNonNull(menuPluginPanels, "menuPluginPanels");

    final ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverviewModeling.MENU_PATH);

    this.setText(labels.getString("viewMenu.text"));
    this.setToolTipText(labels.getString("viewMenu.tooltipText"));
    this.setMnemonic('V');

    // Menu item View -> Add Background Image
    menuAddBitmap = new JMenuItem(actionMap.get(AddBitmapAction.ID));
    add(menuAddBitmap);

    addSeparator();

    List<Action> viewActions = createToolBarActions();
    if (!viewActions.isEmpty()) {
      menuViewToolBars = new ViewToolBarsMenu(viewActions);
      add(menuViewToolBars);
    }
    else {
      menuViewToolBars = null;
    }

    // Menu item View -> Plugins
    this.menuPluginPanels = menuPluginPanels;
    menuPluginPanels.setOperationMode(OperationMode.MODELLING);
    add(menuPluginPanels);

    // Menu item View -> Restore docking layout
    menuItemRestoreDockingLayout = new JMenuItem(actionMap.get(RestoreDockingLayoutAction.ID));
    menuItemRestoreDockingLayout.setText(labels.getString("viewMenu.menuItem_restoreWindowArrangement.text"));
    add(menuItemRestoreDockingLayout);
  }

  private List<Action> createToolBarActions() {
    List<Action> toolBarActions = new ArrayList<>();
    for (JToolBar curToolBar : toolBarManager.getToolBars()) {
      toolBarActions.add(new ToggleVisibleAction(curToolBar, curToolBar.getName()));
    }
    return toolBarActions;
  }
}
