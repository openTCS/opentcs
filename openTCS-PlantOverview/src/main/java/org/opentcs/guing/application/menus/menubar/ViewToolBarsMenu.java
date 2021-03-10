/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.menus.menubar;

import java.util.Collection;
import static java.util.Objects.requireNonNull;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ViewToolBarsMenu
    extends JMenu {

  private static final ResourceBundleUtil labelsMenu = 
    ResourceBundleUtil.getBundle(I18nPlantOverview.MENU_PATH);
  private static final ResourceBundleUtil labelsToolbar = 
    ResourceBundleUtil.getBundle(I18nPlantOverview.TOOLBAR_PATH);

  public ViewToolBarsMenu(Collection<Action> viewActions) {
    super(labelsMenu.getString("viewToolBarsMenu.text"));
    requireNonNull(viewActions, "viewActions");

    JCheckBoxMenuItem checkBoxMenuItem;
    for (Action a : viewActions) {
      checkBoxMenuItem = new JCheckBoxMenuItem(a);
      add(checkBoxMenuItem);

      if (checkBoxMenuItem.getText().equals(labelsToolbar.getString("toolBarManager.toolbar_drawing.title"))) {
        checkBoxMenuItem.setEnabled(false); // "Draw"-Toolbar musn't be disabled.
      }
    }

  }

  public void setOperationMode(OperationMode mode) {
    requireNonNull(mode, "mode");

    setEnabled(mode == OperationMode.MODELLING);
  }
}
