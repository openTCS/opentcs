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
 * Provides instances of {@link CoordinateSystemMovementCommandTransformer} and
 * {@link CoordinateSystemIncomingPoseTransformer}.
 */
public class CoordinateSystemTransformerFactory
    implements
      VehicleDataTransformerFactory {

  public CoordinateSystemTransformerFactory() {
  }

  @Override
  @Nonnull
  public String getName() {
    return "OFFSET_TRANSFORMER";
  }

  @Override
  @Nonnull
  public MovementCommandTransformer createMovementCommandTransformer(
      @Nonnull
      Vehicle vehicle
  ) {
    requireNonNull(vehicle);

    return new CoordinateSystemMovementCommandTransformer(
        CoordinateSystemTransformation.fromVehicle(vehicle)
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Cannot create transformer without transformation data."
                )
            )
    );
  }

  @Override
  @Nonnull
  public IncomingPoseTransformer createIncomingPoseTransformer(
      @Nonnull
      Vehicle vehicle
  ) {
    requireNonNull(vehicle);

    return new CoordinateSystemIncomingPoseTransformer(
        CoordinateSystemTransformation.fromVehicle(vehicle)
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Cannot create transformer without transformation data."
                )
            )
    );
  }

  @Override
  public boolean providesTransformersFor(
      @Nonnull
      Vehicle vehicle
  ) {
    requireNonNull(vehicle, "vehicle");

    return CoordinateSystemTransformation.fromVehicle(vehicle).isPresent();
  }
}
