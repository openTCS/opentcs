// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.components.dialogs;

import org.opentcs.guing.base.model.elements.VehicleModel;

/**
 */
public interface SingleVehicleViewFactory {

  /**
   * Creates a new SingleVehicleView for the given model.
   *
   * @param vehicleModel The vehicle model.
   * @return A new SingleVehicleView for the given model.
   */
  SingleVehicleView createSingleVehicleView(VehicleModel vehicleModel);
}
