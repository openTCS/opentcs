// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.application.menus;

import java.util.Collection;
import org.opentcs.guing.base.model.elements.VehicleModel;

/**
 * A factory for various menus and menu items.
 */
public interface MenuFactory {

  /**
   * Creates a popup menu with actions for a set of vehicles.
   *
   * @param vehicles The vehicle models for which to create the popup menu.
   * @return A popup menu with actions for the given vehicle.
   */
  VehiclePopupMenu createVehiclePopupMenu(Collection<VehicleModel> vehicles);
}
