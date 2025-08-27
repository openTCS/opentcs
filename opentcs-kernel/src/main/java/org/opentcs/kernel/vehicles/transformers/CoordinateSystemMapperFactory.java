// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles.transformers;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.IncomingPoseTransformer;
import org.opentcs.drivers.vehicle.MovementCommandTransformer;
import org.opentcs.drivers.vehicle.VehicleDataTransformerFactory;

/**
 * Provides instances of {@link CsmMovementCommandTransformer} and
 * {@link CsmIncomingPoseTransformer}.
 */
public class CoordinateSystemMapperFactory
    implements
      VehicleDataTransformerFactory {

  public CoordinateSystemMapperFactory() {
  }

  @Nonnull
  @Override
  public String getName() {
    return "COORDINATE_SYSTEM_MAPPER";
  }

  @Nonnull
  @Override
  public MovementCommandTransformer createMovementCommandTransformer(
      @Nonnull
      Vehicle vehicle
  ) {
    requireNonNull(vehicle);

    return new CsmMovementCommandTransformer(
        CoordinateSystemMapping.fromVehicle(vehicle)
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Cannot create transformer without transformation data."
                )
            )
    );
  }

  @Nonnull
  @Override
  public IncomingPoseTransformer createIncomingPoseTransformer(
      @Nonnull
      Vehicle vehicle
  ) {
    requireNonNull(vehicle);

    return new CsmIncomingPoseTransformer(
        CoordinateSystemMapping.fromVehicle(vehicle)
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

    return CoordinateSystemMapping.fromVehicle(vehicle).isPresent();
  }
}
