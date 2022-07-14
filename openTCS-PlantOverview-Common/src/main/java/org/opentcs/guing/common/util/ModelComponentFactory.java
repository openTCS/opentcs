/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.util;

import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.base.model.elements.VehicleModel;

/**
 * A factory for ModelComponents.
 *
 * @author Stefan Walter (Fraunhofer IML)
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
