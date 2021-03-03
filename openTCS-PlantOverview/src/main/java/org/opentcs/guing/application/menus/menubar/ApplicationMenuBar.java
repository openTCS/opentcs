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
import javax.swing.JMenuBar;
import net.engio.mbassy.listener.Handler;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.event.OperationModeChangeEvent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * The plant overview's main menu bar.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ApplicationMenuBar
    extends JMenuBar {

  private static final ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
  private final FileMenu menuFile;
  private final EditMenu menuEdit;
  private final ActionsMenu menuActions;
  private final ViewMenu menuView;
  private final HelpMenu menuHelp;

  /**
   * Creates a new instance.
   *
   * @param menuFile The "File" menu.
   * @param menuEdit The "Edit" menu.
   * @param menuActions The "Actions" menu.
   * @param menuView The "View" menu.
   * @param menuHelp The "Help menu.
   */
  @Inject
  public ApplicationMenuBar(FileMenu menuFile,
                            EditMenu menuEdit,
                            ActionsMenu menuActions,
                            ViewMenu menuView,
                            HelpMenu menuHelp) {
    requireNonNull(menuFile, "menuFile");
    requireNonNull(menuEdit, "menuEdit");
    requireNonNull(menuActions, "menuActions");
    requireNonNull(menuView, "menuView");
    requireNonNull(menuHelp, "menuHelp");

    this.menuFile = menuFile;
    labels.configureMenu(menuFile, "file");
    add(menuFile);

    // --- Menu Edit ---
    this.menuEdit = menuEdit;
    labels.configureMenu(menuEdit, "edit");
    add(menuEdit);

    // --- Menu Actions ---
    this.menuActions = menuActions;
    labels.configureMenu(menuActions, "actions");
    add(menuActions);

    // --- Menu View ---
    this.menuView = menuView;
    labels.configureMenu(menuView, "view");
    add(menuView);

    // --- Menu Help ---
    this.menuHelp = menuHelp;
    labels.configureMenu(menuHelp, "help");
    add(menuHelp);
  }

  /**
   * Handles changes of the application's operation mode.
   *
   * @param evt The mode change event.
   */
  @Handler
  public void handleModeChange(OperationModeChangeEvent evt) {
    setOperationMode(evt.getNewMode());
  }

  /**
   * Adjusts this menu bar and its menues for the given mode of operation.
   *
   * @param mode The mode of operation.
   */
  public void setOperationMode(OperationMode mode) {
    requireNonNull(mode, "mode");

    menuFile.setOperationMode(mode);
    menuEdit.setOperationMode(mode);
    menuActions.setOperationMode(mode);
    menuView.setOperationMode(mode);
    menuHelp.setOperationMode(mode);
  }
}
