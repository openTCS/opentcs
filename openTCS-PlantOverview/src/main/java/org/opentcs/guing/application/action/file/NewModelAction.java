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
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.util.ImageDirectory;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class NewModelAction
    extends AbstractAction {

  public final static String ID = "file.newModel";
  private final GuiManager view;

  /**
   * Creates a new instance.
   *
   * @param view The gui manager
   */
  public NewModelAction(GuiManager view) {
    this.view = view;

    ResourceBundleUtil.getBundle().configureAction(this, ID);

    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl N"));
    putValue(MNEMONIC_KEY, Integer.valueOf('N'));

    URL url = getClass().getResource(ImageDirectory.DIR + "/menu/document-new.png");
    putValue(SMALL_ICON, new ImageIcon(url));
    putValue(LARGE_ICON_KEY, new ImageIcon(url));
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    view.createEmptyModel();
  }
}
