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
import org.opentcs.components.plantoverview.LocationTheme;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.application.action.view.LocationThemeAction;
import org.opentcs.guing.util.LocationThemeManager;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ViewLocationThemeMenu
    extends JMenu {

  private static final ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

  @Inject
  public ViewLocationThemeMenu(OpenTCSView view,
                               LocationThemeManager locationThemeManager) {
    super(labels.getString("view.locationTheme.text"));
    requireNonNull(view, "view");
    requireNonNull(locationThemeManager, "locationThemeManager");

    ActionMap actionMap = view.getActionMap();
    
    final ButtonGroup themeGroup = new ButtonGroup();

    JCheckBoxMenuItem checkBoxMenuItem;
    LocationTheme defaultTheme = locationThemeManager.getDefaultConfigStoreTheme();
    Action action = actionMap.get(LocationThemeAction.ID);
    checkBoxMenuItem = new JCheckBoxMenuItem(action);

    themeGroup.add(checkBoxMenuItem);

    ActionUtil.configureJCheckBoxMenuItem(checkBoxMenuItem, action);

    add(checkBoxMenuItem);

    if (defaultTheme == null) {
      checkBoxMenuItem.setSelected(true);
    }

    for (LocationTheme curTheme : locationThemeManager.getThemes()) {
      String id = curTheme.getName();
      action = actionMap.get(id);
      checkBoxMenuItem = new JCheckBoxMenuItem(action);
      themeGroup.add(checkBoxMenuItem);
      ActionUtil.configureJCheckBoxMenuItem(checkBoxMenuItem, action);
      add(checkBoxMenuItem);

      if (defaultTheme == curTheme) {
        checkBoxMenuItem.setSelected(true);
      }
    }
  }

  public void setOperationMode(OperationMode mode) {
    requireNonNull(mode, "mode");
    
  }
}
