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
import org.opentcs.guing.common.application.action.file.ModelPropertiesAction;
import org.opentcs.guing.common.application.action.file.SaveModelAction;
import org.opentcs.guing.common.application.action.file.SaveModelAsAction;
import org.opentcs.operationsdesk.application.action.ViewActionMap;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.opentcs.thirdparty.operationsdesk.jhotdraw.application.action.file.CloseFileAction;

/**
 * The application's "File" menu.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class FileMenu
    extends JMenu {

  /**
   * A menu item for persisting the kernel's current model.
   */
  private final JMenuItem menuItemSaveModel;
  /**
   * A menu item for persisting the kernel's current model with a new name.
   */
  private final JMenuItem menuItemSaveModelAs;
  /**
   * A menu item for showing the current model's properties.
   */
  private final JMenuItem menuItemModelProperties;
  /**
   * A menu item for closing the application.
   */
  private final JMenuItem menuItemClose;

  /**
   * Creates a new instance.
   *
   * @param actionMap The application's action map.
   */
  @Inject
  public FileMenu(ViewActionMap actionMap) {
    requireNonNull(actionMap, "actionMap");

    final ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.MENU_PATH);
    
    this.setText( labels.getString("fileMenu.text"));
    this.setToolTipText(labels.getString("fileMenu.tooltipText"));
    this.setMnemonic('F');

    // Menu item File -> Save Model
    menuItemSaveModel = new JMenuItem(actionMap.get(SaveModelAction.ID));
    add(menuItemSaveModel);

    // Menu item File -> Save Model As
    menuItemSaveModelAs = new JMenuItem(actionMap.get(SaveModelAsAction.ID));
    add(menuItemSaveModelAs);

    addSeparator();

    menuItemModelProperties = new JMenuItem(actionMap.get(ModelPropertiesAction.ID));
    add(menuItemModelProperties);

    addSeparator();

    // Menu item File -> Close
    menuItemClose = new JMenuItem(actionMap.get(CloseFileAction.ID));
    add(menuItemClose); // TODO: Nur bei "Stand-Alone" Frame
  }

}
