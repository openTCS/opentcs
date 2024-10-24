// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.tree.elements;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.common.application.GuiManager;
import org.opentcs.guing.common.persistence.ModelManager;

/**
 * Represents a point object in the TreeView.
 */
public class LayoutUserObject
    extends
      FigureUserObject {

  /**
   * Creates a new instance.
   *
   * @param model The corresponding data object
   * @param guiManager The gui manager.
   * @param modelManager The model manager
   */
  @Inject
  public LayoutUserObject(
      @Assisted
      LayoutModel model,
      GuiManager guiManager,
      ModelManager modelManager
  ) {
    super(model, guiManager, modelManager);
  }

  @Override
  public LayoutModel getModelComponent() {
    return (LayoutModel) super.getModelComponent();
  }
}
