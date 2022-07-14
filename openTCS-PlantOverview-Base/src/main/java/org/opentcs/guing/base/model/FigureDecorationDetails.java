/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.model;

import java.util.Set;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.VehicleModel;

/**
 * Provides methods to configure the data that is used to decorate a model component's figure.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface FigureDecorationDetails {

  /**
   * Adds a vehicle model.
   *
   * @param model The vehicle model.
   */
  void addVehicleModel(VehicleModel model);

  /**
   * Removes a vehicle model.
   *
   * @param model The vehicle model.
   */
  void removeVehicleModel(VehicleModel model);

  /**
   * Returns a set of vehicle models for which a model component's figure is to be decorated to
   * indicate that the model component is part of the route of the respective vehicles.
   *
   * @return A set of vehicle models.
   */
  Set<VehicleModel> getVehicleModels();

  /**
   * Adds a block model.
   *
   * @param model The block model.
   */
  void addBlockModel(BlockModel model);

  /**
   * Removes a block model.
   *
   * @param model The block model.
   */
  void removeBlockModel(BlockModel model);

  /**
   * Returns a set of block models for which a model component's figure is to be decorated to
   * indicate that the model component is part of the respective block.
   *
   * @return A set of block models.
   */
  Set<BlockModel> getBlockModels();
}
