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
import org.opentcs.operationsdesk.application.action.ViewActionMap;
import org.opentcs.operationsdesk.application.action.app.AboutAction;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * The application's "Help" menu.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class HelpMenu
    extends JMenu {

  /**
   * A menu item for showing the application's "about" panel.
   */
  private final JMenuItem menuItemAbout;

  /**
   * Creates a new instance.
   *
   * @param actionMap The application's action map.
   */
  @Inject
  public HelpMenu(ViewActionMap actionMap) {
    requireNonNull(actionMap, "actionMap");

    final ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.MENU_PATH);

    this.setText(labels.getString("helpMenu.text"));
    this.setToolTipText(labels.getString("helpMenu.tooltipText"));
    this.setMnemonic('?');

    menuItemAbout = add(actionMap.get(AboutAction.ID));
  }

}
