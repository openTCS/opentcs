/*
 * openTCS copyright information:
 * Copyright (c) 2009 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers;

import org.opentcs.access.LocalKernel;
import org.opentcs.data.model.Vehicle;

/**
 * Provides communication adapter instances for vehicles to be controlled.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface CommunicationAdapterFactory {

  /**
   * Sets the communication adapter registry the factory is registered with.
   *
   * @param registry The registry.
   * @deprecated Replaced with setKernel(). Will be removed after openTCS 2.7.
   */
  @Deprecated
  void setCommAdapterRegistry(CommunicationAdapterRegistry registry);
  
  /**
   * Sets a reference to the kernel.
   *
   * @param kernel The kernel.
   */
  void setKernel(LocalKernel kernel);

  /**
   * Returns a string describing the factory/the adapters provided.
   * This should be a short string that can be displayed e.g. as a menu item for
   * choosing between multiple factories/adapter types for a vehicle.
   *
   * @return A string describing the factory/the adapters created.
   */
  String getAdapterDescription();

  /**
   * Checks whether this factory can provide a communication adapter for the
   * given vehicle.
   *
   * @param vehicle The vehicle to check for.
   * @return <code>true</code> if, and only if, this factory can provide a
   * communication adapter to control the given vehicle.
   */
  boolean providesAdapterFor(Vehicle vehicle);

  /**
   * Returns a communication adapter for controlling the given vehicle.
   *
   * @param vehicle The vehicle to be controlled.
   * @return A communication adapter for controlling the given vehicle, or
   * <code>null</code>, if this factory cannot provide an adapter for it.
   */
  BasicCommunicationAdapter getAdapterFor(Vehicle vehicle);
}
