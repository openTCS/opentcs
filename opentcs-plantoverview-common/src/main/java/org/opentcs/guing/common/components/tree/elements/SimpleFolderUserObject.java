// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.tree.elements;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import javax.swing.JComponent;
import org.opentcs.guing.base.model.CompositeModelComponent;
import org.opentcs.guing.common.application.GuiManager;
import org.opentcs.guing.common.persistence.ModelManager;

/**
 * A folder in the TreeView with no added functionality.
 */
public class SimpleFolderUserObject
    extends
      AbstractUserObject {

  /**
   * Creates a new instance.
   *
   * @param dataObject The associated model component.
   * @param guiManager The gui manager.
   * @param modelManager Provides access to the currently loaded system model.
   */
  @Inject
  public SimpleFolderUserObject(
      @Assisted
      CompositeModelComponent dataObject,
      GuiManager guiManager,
      ModelManager modelManager
  ) {
    super(dataObject, guiManager, modelManager);
  }

  @Override // AbstractUserObject
  public boolean removed() {
    return false;
  }

  @Override // AbstractUserObject
  public void rightClicked(JComponent component, int x, int y) {
    // Empty - no popup menu to be displayed.
  }
}
