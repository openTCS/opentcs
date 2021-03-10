/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.file;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.opentcs.guing.application.GuiManager;
import static org.opentcs.guing.util.I18nPlantOverview.MENU_PATH;
import org.opentcs.guing.util.ImageDirectory;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class SaveModelAsAction
    extends AbstractAction {

  public final static String ID = "file.saveModelAs";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);
  /**
   * The manager this instance is working with.
   */
  private final GuiManager guiManager;

  /**
   * Creates a new instance.
   *
   * @param manager The gui manager
   */
  public SaveModelAsAction(final GuiManager manager) {
    this.guiManager = manager;

    putValue(NAME, BUNDLE.getString("saveModelAsAction.name"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("saveModelAsAction.shortDescription"));
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("shift ctrl S"));
    putValue(MNEMONIC_KEY, Integer.valueOf('A'));

    ImageIcon icon = ImageDirectory.getImageIcon("/menu/document-save-as.png");
    putValue(SMALL_ICON, icon);
    putValue(LARGE_ICON_KEY, icon);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    guiManager.saveModelAs();
  }
}
