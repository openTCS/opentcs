/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.dialogs;

import org.opentcs.guing.model.elements.VehicleModel;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
interface SingleVehicleViewFactory {
  
  /**
   * Creates a new SingleVehicleView for the given model.
   *
   * @param vehicleModel The vehicle model.
   * @return A new SingleVehicleView for the given model.
   */
  SingleVehicleView createSingleVehicleView(VehicleModel vehicleModel);
}
