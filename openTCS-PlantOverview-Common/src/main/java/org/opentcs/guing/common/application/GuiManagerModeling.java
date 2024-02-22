/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.application;

import javax.annotation.Nonnull;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.base.model.elements.VehicleModel;

/**
 * Provides services concerning model editing.
 */
public interface GuiManagerModeling
    extends GuiManager {

  /**
   * Creates a new vehicle model.
   *
   * @return The created vehicle model.
   */
  VehicleModel createVehicleModel();

  /**
   * Creates a new location type model.
   *
   * @return The created location type model.
   */
  LocationTypeModel createLocationTypeModel();

  /**
   * Creates a new block model.
   *
   * @return The created block model.
   */
  BlockModel createBlockModel();

  /**
   * Removes a block model.
   *
   * <p>
   * This method is primarily provided for use in plugin panels.
   * </p>
   *
   * @param blockModel The block model to be removed.
   */
  void removeBlockModel(@Nonnull BlockModel blockModel);
}
