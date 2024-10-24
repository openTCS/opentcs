// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.util;

import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.base.model.elements.VehicleModel;

/**
 * A factory for ModelComponents.
 */
public class ModelComponentFactory {


  /**
   * Creates a new instance.
   */
  public ModelComponentFactory() {
  }

  public LayoutModel createLayoutModel() {
    return new LayoutModel();
  }

  public VehicleModel createVehicleModel() {
    return new VehicleModel();
  }

  public LocationTypeModel createLocationTypeModel() {
    return new LocationTypeModel();
  }

  public BlockModel createBlockModel() {
    return new BlockModel();
  }
}
