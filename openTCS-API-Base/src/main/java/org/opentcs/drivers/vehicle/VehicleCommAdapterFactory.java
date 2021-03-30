/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.Vehicle;

/**
 * Provides communication adapter instances for vehicles to be controlled.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface VehicleCommAdapterFactory
    extends Lifecycle {

  /**
   * Returns a {@link VehicleCommAdapterDescription} for the factory/the adapters provided.
   *
   * @return A {@link VehicleCommAdapterDescription} for the factory/the adapters provided.
   */
  VehicleCommAdapterDescription getDescription();

  /**
   * Checks whether this factory can provide a communication adapter for the
   * given vehicle.
   *
   * @param vehicle The vehicle to check for.
   * @return <code>true</code> if, and only if, this factory can provide a
   * communication adapter to control the given vehicle.
   */
  boolean providesAdapterFor(@Nonnull Vehicle vehicle);

  /**
   * Returns a communication adapter for controlling the given vehicle.
   *
   * @param vehicle The vehicle to be controlled.
   * @return A communication adapter for controlling the given vehicle, or
   * <code>null</code>, if this factory cannot provide an adapter for it.
   */
  @Nullable
  VehicleCommAdapter getAdapterFor(@Nonnull Vehicle vehicle);
}
