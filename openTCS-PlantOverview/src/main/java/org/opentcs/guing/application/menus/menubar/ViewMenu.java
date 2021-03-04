/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.menus.menubar;

import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.application.action.ViewActionMap;
import org.opentcs.guing.application.action.view.AddBitmapAction;
import org.opentcs.guing.application.action.view.AddDrawingViewAction;
import org.opentcs.guing.application.action.view.AddTransportOrderSequenceView;
import org.opentcs.guing.application.action.view.AddTransportOrderView;
import org.opentcs.guing.application.action.view.RestoreDockingLayoutAction;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * The application's menu for view-related operations.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ViewMenu
    extends JMenu {

  /**
   * A menu item for setting a bitmap for the current drawing view.
   */
  private final JMenuItem menuAddBitmap;
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
   * @param view The application's main view.
   * @param menuPluginPanels A menu for showing/hiding plugin panels.
   */
  @Inject
  public ViewMenu(ViewActionMap actionMap,
                  OpenTCSView view,
                  ViewPluginPanelsMenu menuPluginPanels) {
    requireNonNull(actionMap, "actionMap");
    requireNonNull(view, "view");
    requireNonNull(menuPluginPanels, "menuPluginPanels");

    final ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

    // Menu item View -> Add Background Image
    menuAddBitmap = new JMenuItem(actionMap.get(AddBitmapAction.ID));
    labels.configureMenu(menuAddBitmap, "view.addBitmap");
    add(menuAddBitmap);

    addSeparator();

    // Menu item View -> Add course view
    menuAddDrawingView = new JMenuItem(actionMap.get(AddDrawingViewAction.ID));
    labels.configureMenu(menuAddDrawingView, "view.drawingView");
    add(menuAddDrawingView);

    // Menu item View -> Add transport order view
    menuTransportOrderView = new JMenuItem(actionMap.get(AddTransportOrderView.ID));
    labels.configureMenu(menuTransportOrderView, "view.transportOrderView");
    add(menuTransportOrderView);

    // Menu item View -> Add transport order sequence view
    menuOrderSequenceView = new JMenuItem(actionMap.get(AddTransportOrderSequenceView.ID));
    labels.configureMenu(menuOrderSequenceView, "view.orderSequenceView");
    add(menuOrderSequenceView);

    addSeparator();

    // Popup menu View -> Toolbars: Show/hide single toolbars
    // The ToolBarActions are set in OpenTCSView.wrapViewComponent().
    // Therefore createToolBars() has to be called() first.
    Object object = view.getComponent().getClientProperty(OpenTCSView.TOOLBAR_ACTIONS_PROPERTY);

    @SuppressWarnings("unchecked")
    List<Action> viewActions = (List<Action>) object;

    if (viewActions != null && !viewActions.isEmpty()) {
      menuViewToolBars = new ViewToolBarsMenu(viewActions);
      add(menuViewToolBars);
    }
    else {
      menuViewToolBars = null;
    }

    // Menu item View -> Plugins
    this.menuPluginPanels = menuPluginPanels;
    add(menuPluginPanels);

    // Menu item View -> Restore docking layout
    menuItemRestoreDockingLayout = new JMenuItem(actionMap.get(RestoreDockingLayoutAction.ID));
    menuItemRestoreDockingLayout.setText(labels.getString("view.restoreDockingLayout.text"));
    add(menuItemRestoreDockingLayout);
  }

  /**
   * Updates the menu's items for the given mode of operation.
   *
   * @param mode The new mode of operation.
   */
  public void setOperationMode(OperationMode mode) {
    requireNonNull(mode, "mode");

    menuOrderSequenceView.setEnabled(mode == OperationMode.OPERATING);
    menuTransportOrderView.setEnabled(mode == OperationMode.OPERATING);
    menuAddDrawingView.setEnabled(mode == OperationMode.OPERATING);
    menuAddBitmap.setEnabled(mode == OperationMode.MODELLING);

    if (menuViewToolBars != null) {
      menuViewToolBars.setOperationMode(mode);
    }

    menuPluginPanels.setOperationMode(mode);
  }

}
