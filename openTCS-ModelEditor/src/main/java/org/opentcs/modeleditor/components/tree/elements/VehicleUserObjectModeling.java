/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.components.tree.elements;

import com.google.inject.assistedinject.Assisted;
import javax.inject.Inject;
import javax.swing.JPopupMenu;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.application.GuiManager;
import org.opentcs.guing.common.components.tree.elements.VehicleUserObject;
import org.opentcs.guing.common.persistence.ModelManager;

/**
 * A Vehicle object in the tree view.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class VehicleUserObjectModeling
    extends VehicleUserObject {

  /**
   * Creates a new instance.
   *
   * @param model The corresponding vehicle object.
   * @param guiManager The gui manager.
   * @param modelManager Provides the current system model.
   */
  @Inject
  public VehicleUserObjectModeling(@Assisted VehicleModel model,
                                   GuiManager guiManager,
                                   ModelManager modelManager) {
    super(model, guiManager, modelManager);
  }

  @Override  // AbstractUserObject
  public JPopupMenu getPopupMenu() {
    return null;
  }
}
