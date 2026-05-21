// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel.routing;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import org.opentcs.data.model.PlantModel;

/**
 * Holds contextual information related to the computation of routing graphs and routes.
 * <p>
 * Provides:
 * </p>
 * <ul>
 * <li>The plant model, whose data serves as the basis for all computation.</li>
 * </ul>
 * <p>
 * If the routing context or any information in it changes, re-computations of routing graphs or
 * routes may be necessary.
 * </p>
 */
public class RoutingContext {

  private final PlantModel plantModel;

  /**
   * Creates a new instance.
   *
   * @param plantModel The plant model of the routing context.
   */
  public RoutingContext(
      @Nonnull
      PlantModel plantModel
  ) {
    this.plantModel = requireNonNull(plantModel, "plantModel");
  }

  /**
   * Returns the plant model of this routing context.
   *
   * @return The plant model of this routing context.
   */
  public PlantModel getPlantModel() {
    return plantModel;
  }
}
