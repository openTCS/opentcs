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
import org.opentcs.guing.common.application.action.file.ModelPropertiesAction;
import org.opentcs.guing.common.application.action.file.SaveModelAction;
import org.opentcs.guing.common.application.action.file.SaveModelAsAction;
import org.opentcs.modeleditor.application.action.ViewActionMap;
import org.opentcs.modeleditor.application.action.file.DownloadModelFromKernelAction;
import org.opentcs.modeleditor.application.action.file.LoadModelAction;
import org.opentcs.modeleditor.application.action.file.NewModelAction;
import org.opentcs.modeleditor.application.action.file.UploadModelToKernelAction;
import org.opentcs.modeleditor.util.I18nPlantOverviewModeling;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.opentcs.thirdparty.modeleditor.jhotdraw.application.action.file.CloseFileAction;

/**
 * The application's "File" menu.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class FileMenu
    extends JMenu {

  /**
   * A menu item for creating a new, empty system model.
   */
  private final JMenuItem menuItemNewModel;
  /**
   * A menu item for loading a mode into the kernel.
   */
  private final JMenuItem menuItemLoadModel;
  /**
   * A menu item for saving the kernel's current model.
   */
  private final JMenuItem menuItemSaveModel;
  /**
   * A menu item for saving the kernel's current model with a new name.
   */
  private final JMenuItem menuItemSaveModelAs;
  /**
   * A menu item for retrieving the system model data from the kernel.
   */
  private final JMenuItem menuItemDownloadModelFromKernel;
  /**
   * A menu item for transferring the system model data to the kernel.
   */
  private final JMenuItem menuItemUploadModelToKernel;
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
   * @param menuImport The sub-menu for the selectable plant model importers.
   * @param menuExport The sub-menu for the selectable plant model exporters.
   */
  @Inject
  public FileMenu(ViewActionMap actionMap,
                  FileImportMenu menuImport,
                  FileExportMenu menuExport) {
    requireNonNull(actionMap, "actionMap");
    requireNonNull(menuImport, "menuImport");
    requireNonNull(menuExport, "menuExport");

    final ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverviewModeling.MENU_PATH);

    this.setText(labels.getString("fileMenu.text"));
    this.setToolTipText(labels.getString("fileMenu.tooltipText"));
    this.setMnemonic('F');

    // Menu item File -> New Model
    menuItemNewModel = new JMenuItem(actionMap.get(NewModelAction.ID));
    add(menuItemNewModel);

    // Menu item File -> Load Model
    menuItemLoadModel = new JMenuItem(actionMap.get(LoadModelAction.ID));
    add(menuItemLoadModel);

    // Menu item File -> Save Model
    menuItemSaveModel = new JMenuItem(actionMap.get(SaveModelAction.ID));
    add(menuItemSaveModel);

    // Menu item File -> Save Model As
    menuItemSaveModelAs = new JMenuItem(actionMap.get(SaveModelAsAction.ID));
    add(menuItemSaveModelAs);

    addSeparator();

    add(menuImport);
    add(menuExport);

    addSeparator();

    // Load model from kernel
    menuItemDownloadModelFromKernel = new JMenuItem(actionMap.get(DownloadModelFromKernelAction.ID));
    add(menuItemDownloadModelFromKernel);

    // Persist model in kernel
    menuItemUploadModelToKernel = new JMenuItem(actionMap.get(UploadModelToKernelAction.ID));
    add(menuItemUploadModelToKernel);

    addSeparator();

    menuItemModelProperties = new JMenuItem(actionMap.get(ModelPropertiesAction.ID));
    add(menuItemModelProperties);

    addSeparator();

    // Menu item File -> Close
    menuItemClose = new JMenuItem(actionMap.get(CloseFileAction.ID));
    add(menuItemClose);
  }

}
