/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.tree.elements;

import com.google.inject.assistedinject.Assisted;
import javax.inject.Inject;
import javax.swing.ImageIcon;
import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.common.application.GuiManager;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.util.IconToolkit;

/**
 * Represents a link in the TreeView.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LinkUserObject
    extends FigureUserObject {

  /**
   * Creates a new instance of LinkUserObject
   *
   * @param modelComponent The corresponding data object
   * @param guiManager The gui manager.
   * @param modelManager The model manager
   */
  @Inject
  public LinkUserObject(@Assisted LinkModel modelComponent,
                        GuiManager guiManager,
                        ModelManager modelManager) {
    super(modelComponent, guiManager, modelManager);
  }

  @Override
  public LinkModel getModelComponent() {
    return (LinkModel) super.getModelComponent();
  }

  @Override
  public ImageIcon getIcon() {
    return IconToolkit.instance().createImageIcon("tree/link.18x18.png");
  }
}
