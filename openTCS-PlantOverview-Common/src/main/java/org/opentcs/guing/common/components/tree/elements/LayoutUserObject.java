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
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.common.application.GuiManager;
import org.opentcs.guing.common.persistence.ModelManager;

/**
 * Represents a point object in the TreeView.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LayoutUserObject
    extends FigureUserObject {

  /**
   * Creates a new instance.
   *
   * @param model The corresponding data object
   * @param guiManager The gui manager.
   * @param modelManager The model manager
   */
  @Inject
  public LayoutUserObject(@Assisted LayoutModel model,
                          GuiManager guiManager,
                          ModelManager modelManager) {
    super(model, guiManager, modelManager);
  }

  @Override
  public LayoutModel getModelComponent() {
    return (LayoutModel) super.getModelComponent();
  }
}
