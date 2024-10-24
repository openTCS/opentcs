// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.components.tree.elements;

import static java.util.Objects.requireNonNull;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import javax.swing.JPopupMenu;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.application.GuiManager;
import org.opentcs.guing.common.components.tree.elements.VehicleUserObject;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.operationsdesk.application.menus.MenuFactory;

/**
 * A Vehicle in the tree view.
 */
public class VehicleUserObjectOperating
    extends
      VehicleUserObject {

  /**
   * A factory for popup menus.
   */
  private final MenuFactory menuFactory;

  /**
   * Creates a new instance.
   *
   * @param model The corresponding vehicle object.
   * @param guiManager The gui manager.
   * @param modelManager Provides the current system model.
   * @param menuFactory A factory for popup menus.
   */
  @Inject
  public VehicleUserObjectOperating(
      @Assisted
      VehicleModel model,
      GuiManager guiManager,
      ModelManager modelManager,
      MenuFactory menuFactory
  ) {
    super(model, guiManager, modelManager);
    this.menuFactory = requireNonNull(menuFactory, "menuFactory");
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return menuFactory.createVehiclePopupMenu(selectedVehicles);
  }
}
