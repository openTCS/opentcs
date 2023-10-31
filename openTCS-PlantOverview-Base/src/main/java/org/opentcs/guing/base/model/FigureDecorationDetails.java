/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.model;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.guing.base.AllocationState;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.VehicleModel;

/**
 * Provides methods to configure the data that is used to decorate a model component's figure.
 */
public interface FigureDecorationDetails {

  /**
   * Returns a map of vehicles that claim or allocate the resource (the figure is associated with)
   * to the respective allocation state.
   * <p>
   * This information is used to decorate a model component's figure to indicate that it is part of
   * the route of the respective vehicles.
   *
   * @return A map of vehicles to allocation states.
   */
  @Nonnull
  Map<VehicleModel, AllocationState> getAllocationStates();

  /**
   * Updates the allocation state for the given vehicle.
   *
   * @param model The vehicle model.
   * @param allocationState The vehicle's new allocation state.
   */
  void updateAllocationState(@Nonnull VehicleModel model, @Nonnull AllocationState allocationState);

  /**
   * Clears the allocation state for the given vehicle.
   *
   * @param model The vehicle model.
   */
  void clearAllocationState(@Nonnull VehicleModel model);

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
