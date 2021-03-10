/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.actions;

import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.AbstractAction;
import static javax.swing.Action.LARGE_ICON_KEY;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.ImageIcon;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.util.ImageDirectory;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to trigger the creation of a block.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class CreateBlockAction
    extends AbstractAction {

  /**
   * This action class's ID.
   */
  public static final String ID = "openTCS.createBlock";
  /**
   * The GUI manager instance we're working with.
   */
  private final GuiManager guiManager;

  /**
   * Creates a new instance.
   *
   * @param guiManager The GUI manager instance we're working with.
   */
  public CreateBlockAction(GuiManager guiManager) {
    this.guiManager = guiManager;
    ResourceBundleUtil.getBundle().configureAction(this, ID);

    URL urlSmall = getClass().getResource(ImageDirectory.DIR + "/toolbar/blockdevice-3.16.png");
    URL urlLarge = getClass().getResource(ImageDirectory.DIR + "/toolbar/blockdevice-3.22.png");
    putValue(SMALL_ICON, new ImageIcon(urlSmall));
    putValue(LARGE_ICON_KEY, new ImageIcon(urlLarge));
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    guiManager.createModelComponent(BlockModel.class);
  }
}
