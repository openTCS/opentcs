/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.dialogs;

import java.util.Collection;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface FindVehiclePanelFactory {

  /**
   * Create a {@link FindVehiclePanel} for the given vehicle models.
   *
   * @param vehicles The vehicle models.
   * @param drawingView The drawing view.
   * @return A {@link FindVehiclePanel}.
   */
  FindVehiclePanel createFindVehiclesPanel(Collection<VehicleModel> vehicles,
                                           OpenTCSDrawingView drawingView);
}
