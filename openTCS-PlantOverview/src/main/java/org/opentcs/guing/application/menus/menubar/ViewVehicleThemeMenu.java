/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.menus.menubar;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import org.jhotdraw.app.action.ActionUtil;
import org.opentcs.components.plantoverview.VehicleTheme;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.application.action.view.VehicleThemeAction;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.VehicleThemeManager;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ViewVehicleThemeMenu
    extends JMenu {

  private static final ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
  
  @Inject
  public ViewVehicleThemeMenu(OpenTCSView view,
                              VehicleThemeManager vehicleThemeManager) {
    super(labels.getString("view.vehicleTheme.text"));
    requireNonNull(view, "view");
    requireNonNull(vehicleThemeManager, "vehicleThemeManager");

    ActionMap actionMap = view.getActionMap();
    
    final ButtonGroup bgVehicleTheme = new ButtonGroup();

    JCheckBoxMenuItem checkBoxMenuItem;
    VehicleTheme defaultVehicleTheme
        = vehicleThemeManager.getDefaultConfigStoreTheme();
    Action action = actionMap.get(VehicleThemeAction.ID);
    checkBoxMenuItem = new JCheckBoxMenuItem(action);

    bgVehicleTheme.add(checkBoxMenuItem);

    ActionUtil.configureJCheckBoxMenuItem(checkBoxMenuItem, action);

    add(checkBoxMenuItem);

    if (defaultVehicleTheme == null) {
      checkBoxMenuItem.setSelected(true);
    }

    for (VehicleTheme curTheme : vehicleThemeManager.getThemes()) {
      String id = curTheme.getName();
      action = actionMap.get(id);
      checkBoxMenuItem = new JCheckBoxMenuItem(action);
      bgVehicleTheme.add(checkBoxMenuItem);
      ActionUtil.configureJCheckBoxMenuItem(checkBoxMenuItem, action);
      add(checkBoxMenuItem);

      if (defaultVehicleTheme == curTheme) {
        checkBoxMenuItem.setSelected(true);
      }
    }
  }

  public void setOperationMode(OperationMode mode) {
    requireNonNull(mode, "mode");

  }
  
}
