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
import javax.swing.JMenuBar;

/**
 * The plant overview's main menu bar.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ApplicationMenuBar
    extends JMenuBar {

  private final FileMenu menuFile;
  private final ActionsMenu menuActions;
  private final ViewMenu menuView;
  private final HelpMenu menuHelp;

  /**
   * Creates a new instance.
   *
   * @param menuFile The "File" menu.
   * @param menuActions The "Actions" menu.
   * @param menuView The "View" menu.
   * @param menuHelp The "Help menu.
   */
  @Inject
  public ApplicationMenuBar(FileMenu menuFile,
                            ActionsMenu menuActions,
                            ViewMenu menuView,
                            HelpMenu menuHelp) {
    requireNonNull(menuFile, "menuFile");
    requireNonNull(menuActions, "menuActions");
    requireNonNull(menuView, "menuView");
    requireNonNull(menuHelp, "menuHelp");

    this.menuFile = menuFile;
    add(menuFile);

    this.menuActions = menuActions;
    add(menuActions);

    this.menuView = menuView;
    add(menuView);

    this.menuHelp = menuHelp;
    add(menuHelp);
  }
}
