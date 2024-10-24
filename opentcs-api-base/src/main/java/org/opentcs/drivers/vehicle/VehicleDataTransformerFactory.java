// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle;

import jakarta.annotation.Nonnull;
import org.opentcs.data.model.Vehicle;

/**
 * Provides matching {@link MovementCommandTransformer} and {@link IncomingPoseTransformer}
 * instances.
 */
public interface VehicleDataTransformerFactory {

  /**
   * Returns the name of this factory.
   *
   * @return The name of this factory.
   */
  @Nonnull
  String getName();

  /**
   * Creates a {@link MovementCommandTransformer} for the given vehicle.
   *
   * @param vehicle The vehicle to create the transformer for.
   * @return The newly created transformer.
   * @throws IllegalArgumentException If a transformer cannot be created for the given vehicle.
   */
  @Nonnull
  MovementCommandTransformer createMovementCommandTransformer(
      @Nonnull
      Vehicle vehicle
  )
      throws IllegalArgumentException;

  /**
   * Creates a {@link IncomingPoseTransformer} for the given vehicle.
   *
   * @param vehicle The vehicle to create the transformer for.
   * @return The newly created transformer.
   * @throws IllegalArgumentException If a transformer cannot be created for the given vehicle.
   */
  @Nonnull
  IncomingPoseTransformer createIncomingPoseTransformer(
      @Nonnull
      Vehicle vehicle
  )
      throws IllegalArgumentException;

  /**
   * Checks if an {@link IncomingPoseTransformer} and {@link MovementCommandTransformer} can be
   * created for the given vehicle.
   *
   * @param vehicle The vehicle to create the transformer for.
   * @return {@code true} when both transformers can be created.
   */
  boolean providesTransformersFor(
      @Nonnull
      Vehicle vehicle
  );
}
