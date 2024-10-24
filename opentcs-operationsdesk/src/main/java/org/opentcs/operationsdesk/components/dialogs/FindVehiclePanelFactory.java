// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.components.dialogs;

import java.util.Collection;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.components.drawing.OpenTCSDrawingView;

/**
 */
public interface FindVehiclePanelFactory {

  /**
   * Create a {@link FindVehiclePanel} for the given vehicle models.
   *
   * @param vehicles The vehicle models.
   * @param drawingView The drawing view.
   * @return A {@link FindVehiclePanel}.
   */
  FindVehiclePanel createFindVehiclesPanel(
      Collection<VehicleModel> vehicles,
      OpenTCSDrawingView drawingView
  );
}
