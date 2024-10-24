// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.components.drawing.figures;

import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.components.drawing.figures.FigureFactory;

/**
 */
public interface VehicleFigureFactory
    extends
      FigureFactory {

  VehicleFigure createVehicleFigure(VehicleModel model);

  NamedVehicleFigure createNamedVehicleFigure(VehicleModel model);
}
