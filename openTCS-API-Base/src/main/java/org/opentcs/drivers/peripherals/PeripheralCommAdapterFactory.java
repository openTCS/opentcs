/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.peripherals;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.Location;

/**
 * Provides communication adapter instances for peripheral devices to be controlled.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface PeripheralCommAdapterFactory
    extends Lifecycle {

  /**
   * Returns a {@link PeripheralCommAdapterDescription} for the factory/the adapters provided.
   *
   * @return A {@link PeripheralCommAdapterDescription} for the factory/the adapters provided.
   */
  @Nonnull
  PeripheralCommAdapterDescription getDescription();

  /**
   * Checks whether this factory can provide a communication adapter for the given
   * location/peripheral device.
   *
   * @param location The location to check for.
   * @return {@code true} if, and only if, this factory can provide a communication adapter to
   * control the given location/peripheral device.
   */
  boolean providesAdapterFor(@Nonnull Location location);

  /**
   * Returns a communication adapter for controlling the given location/peripheral device.
   *
   * @param location The location/peripheral device to be controlled.
   * @return A communication adapter for controlling the given location/peripheral device, or
   * {@code null}, if this factory cannot provide an adapter for it.
   */
  @Nullable
  PeripheralCommAdapter getAdapterFor(@Nonnull Location location);
}
