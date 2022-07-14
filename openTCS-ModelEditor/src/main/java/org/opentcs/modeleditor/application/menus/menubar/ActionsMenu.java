/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.application.menus.menubar;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opentcs.guing.common.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.common.util.PlantOverviewApplicationConfiguration;
import org.opentcs.modeleditor.application.action.ViewActionMap;
import org.opentcs.modeleditor.application.menus.MenuFactory;
import org.opentcs.modeleditor.util.I18nPlantOverviewModeling;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * The application's menu for run-time actions.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ActionsMenu
    extends JMenu {

  /**
   * A menu item for assuming the model coordinates from the layout coordinates.
   */
  private final JMenuItem cbiAlignLayoutWithModel;
  /**
   * A menu item for assuming the layout coordinates from the model coordinates.
   */
  private final JMenuItem cbiAlignModelWithLayout;

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

    final ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverviewModeling.MENU_PATH);

    this.setText(labels.getString("actionsMenu.text"));
    this.setToolTipText(labels.getString("actionsMenu.tooltipText"));
    this.setMnemonic('A');

    // Menu item Actions -> Copy model to layout
    cbiAlignModelWithLayout = menuFactory.createModelToLayoutMenuItem(true);
    add(cbiAlignModelWithLayout);

    // Menu item Actions -> Copy layout to model
    cbiAlignLayoutWithModel = menuFactory.createLayoutToModelMenuItem(true);
    add(cbiAlignLayoutWithModel);
  }

}
