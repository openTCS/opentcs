// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.application.action.file;

import static org.opentcs.modeleditor.util.I18nPlantOverviewModeling.MENU_PATH;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.opentcs.guing.common.application.GuiManager;
import org.opentcs.guing.common.util.ImageDirectory;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 */
public class LoadModelAction
    extends
      AbstractAction {

  /**
   * This action's ID.
   */
  public static final String ID = "file.loadModel";
  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);
  private final GuiManager view;

  /**
   * Creates a new instance.
   *
   * @param view The gui manager
   */
  @SuppressWarnings("this-escape")
  public LoadModelAction(GuiManager view) {
    this.view = view;

    putValue(NAME, BUNDLE.getString("loadModelAction.name"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("loadModelAction.shortDescription"));
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl L"));
    putValue(MNEMONIC_KEY, Integer.valueOf('L'));

    ImageIcon icon = ImageDirectory.getImageIcon("/menu/document-import-2.png");
    putValue(SMALL_ICON, icon);
    putValue(LARGE_ICON_KEY, icon);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    view.loadModel();
  }
}
