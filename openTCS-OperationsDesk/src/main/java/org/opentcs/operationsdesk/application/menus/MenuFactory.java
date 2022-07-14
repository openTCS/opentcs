/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.application.menus;

import java.util.Collection;
import org.opentcs.guing.base.model.elements.VehicleModel;

/**
 * A factory for various menus and menu items.
 *
 * @author Stefan Walter (Fraunhofer IML)
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
