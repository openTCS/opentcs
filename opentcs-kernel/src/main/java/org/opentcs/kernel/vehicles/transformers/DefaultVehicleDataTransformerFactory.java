/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles.transformers;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.IncomingPoseTransformer;
import org.opentcs.drivers.vehicle.MovementCommandTransformer;
import org.opentcs.drivers.vehicle.VehicleDataTransformerFactory;

/**
 * Provides transformers that do not modify their inputs in any way.
 */
public class DefaultVehicleDataTransformerFactory
    implements
      VehicleDataTransformerFactory {

  public DefaultVehicleDataTransformerFactory() {
  }

  @Override
  @Nonnull
  public String getName() {
    return "DEFAULT_TRANSFORMER";
  }

  @Override
  @Nonnull
  public MovementCommandTransformer createMovementCommandTransformer(
      @Nonnull
      Vehicle vehicle
  ) {
    requireNonNull(vehicle, "vehicle");

    return command -> command;
  }

  @Override
  @Nonnull
  public IncomingPoseTransformer createIncomingPoseTransformer(
      @Nonnull
      Vehicle vehicle
  ) {
    requireNonNull(vehicle, "vehicle");

    return pose -> pose;
  }

  @Override
  public boolean providesTransformersFor(
      @Nonnull
      Vehicle vehicle
  ) {
    requireNonNull(vehicle, "vehicle");

    return true;
  }
}
