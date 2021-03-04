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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.application.action.ViewActionMap;
import org.opentcs.guing.application.action.file.ModelPropertiesAction;
import org.opentcs.guing.application.action.file.CloseFileAction;
import org.opentcs.guing.application.action.file.LoadModelAction;
import org.opentcs.guing.application.action.file.NewModelAction;
import org.opentcs.guing.application.action.file.SaveModelAction;
import org.opentcs.guing.application.action.file.SaveModelAsAction;
import org.opentcs.guing.application.action.synchronize.LoadModelFromKernelAction;
import org.opentcs.guing.application.action.synchronize.PersistInKernelAction;
import org.opentcs.guing.util.ResourceBundleUtil;

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
   * A menu item for persisting the kernel's current model.
   */
  private final JMenuItem menuItemSaveModel;
  /**
   * A menu item for persisting the kernel's current model with a new name.
   */
  private final JMenuItem menuItemSaveModelAs;
  /**
   * A submenu for importers.
   */
  private final FileImportMenu menuImport;
  /**
   * A submenu for exporters.
   */
  private final FileExportMenu menuExport;
  /**
   * A menu item for retrieving the system model data from the kernel.
   */
  private final JMenuItem menuItemLoadModelFromKernel;
  /**
   * A menu item for transferring the system model data to the kernel.
   */
  private final JMenuItem menuItemPersistInKernel;
  /**
   * A sub-menu for setting the application's mode of operation.
   */
  private final FileModeMenu menuMode;
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
   * @param menuMode The sub-menu for the application's mode of operation.
   * @param menuImport The sub-menu for the selectable plant model importers.
   * @param menuExport The sub-menu for the selectable plant model exporters.
   */
  @Inject
  public FileMenu(ViewActionMap actionMap,
                  FileModeMenu menuMode,
                  FileImportMenu menuImport,
                  FileExportMenu menuExport) {
    requireNonNull(actionMap, "actionMap");
    this.menuMode = requireNonNull(menuMode, "menuMode");
    this.menuImport = requireNonNull(menuImport, "menuImport");
    this.menuExport = requireNonNull(menuExport, "menuExport");

    final ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

    // Menu item File -> New Model (nur in Mode Modelling)
    menuItemNewModel = new JMenuItem(actionMap.get(NewModelAction.ID));
    labels.configureMenu(menuItemNewModel, NewModelAction.ID);
    add(menuItemNewModel);

    // Menu item File -> Load Model (nur in Mode Modelling)
    menuItemLoadModel = new JMenuItem(actionMap.get(LoadModelAction.ID));
    labels.configureMenu(menuItemLoadModel, LoadModelAction.ID);
    add(menuItemLoadModel);

    // Menu item File -> Save Model
    menuItemSaveModel = new JMenuItem(actionMap.get(SaveModelAction.ID));
    labels.configureMenu(menuItemSaveModel, SaveModelAction.ID);
    add(menuItemSaveModel);

    // Menu item File -> Save Model As
    menuItemSaveModelAs = new JMenuItem(actionMap.get(SaveModelAsAction.ID));
    labels.configureMenu(menuItemSaveModelAs, SaveModelAsAction.ID);
    add(menuItemSaveModelAs);

    addSeparator();

    add(menuImport);
    add(menuExport);

    addSeparator();

    // Load model from kernel
    menuItemLoadModelFromKernel = new JMenuItem(actionMap.get(LoadModelFromKernelAction.ID));
    labels.configureMenu(menuItemLoadModelFromKernel, LoadModelFromKernelAction.ID);
    add(menuItemLoadModelFromKernel);

    // Persist model in kernel
    menuItemPersistInKernel = new JMenuItem(actionMap.get(PersistInKernelAction.ID));
    labels.configureMenu(menuItemPersistInKernel, PersistInKernelAction.ID);
    add(menuItemPersistInKernel);

    add(menuMode);

    addSeparator();

    menuItemModelProperties = new JMenuItem(actionMap.get(ModelPropertiesAction.ID));
    labels.configureMenu(menuItemModelProperties, ModelPropertiesAction.ID);
    add(menuItemModelProperties);

    addSeparator();

    // Menu item File -> Close
    menuItemClose = new JMenuItem(actionMap.get(CloseFileAction.ID));
    labels.configureMenu(menuItemClose, CloseFileAction.ID);
    add(menuItemClose); // TODO: Nur bei "Stand-Alone" Frame
  }

  /**
   * Updates the menu's items for the given mode of operation.
   *
   * @param mode The new mode of operation.
   */
  public void setOperationMode(OperationMode mode) {
    requireNonNull(mode, "mode");

    menuItemNewModel.setEnabled(mode == OperationMode.MODELLING);
    menuItemLoadModel.setEnabled(mode == OperationMode.MODELLING);
    menuMode.setOperationMode(mode);
    // Saving is also allowed in OPERATING mode, e.g. locking of Pathes
    menuItemSaveModel.setEnabled(true);
    menuItemSaveModelAs.setEnabled(true);

    menuImport.setEnabled(mode == OperationMode.MODELLING);
    menuExport.setEnabled(mode == OperationMode.MODELLING);

    menuItemLoadModelFromKernel.setEnabled(mode == OperationMode.MODELLING);
    menuItemPersistInKernel.setEnabled(mode == OperationMode.MODELLING);
  }

}
