/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.components.dialogs;

import java.util.Collection;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.components.drawing.OpenTCSDrawingView;

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
