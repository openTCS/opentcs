/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel;

import javax.annotation.Nonnull;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.Location;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * This interface declares the methods a peripheral job dispatcher module for the openTCS kernel
 * must implement.
 * <p>
 * A peripheral job dispatcher manages the distribution of peripheral jobs among the peripheral
 * devices represented by locations in a system. It is basically event-driven, where an event can
 * be a new peripheral job being introduced into the system or a peripheral device becoming
 * available for processing existing jobs.
 * </p>
 */
public interface PeripheralJobDispatcher
    extends Lifecycle {

  /**
   * Notifies the dispatcher that it should start the dispatching process.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   */
  void dispatch();

  /**
   * Notifies the dispatcher that any job a peripheral device (represented by the given location)
   * might be processing is to be withdrawn.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param location The location representing a peripheral device whose job is withdrawn.
   * @throws IllegalArgumentException If the given peripheral's current job is already in a final
   * state, or if it is related to a transport order and this transport order is not in a final
   * state.
   */
  void withdrawJob(@Nonnull Location location)
      throws IllegalArgumentException;

  /**
   * Notifies the dispatcher that the given peripheral job is to be withdrawn.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param job The job to be withdrawn.
   * @throws IllegalArgumentException If the given peripheral job is already in a final state, or if
   * it is related to a transport order and this transport order is not in a final state.
   */
  void withdrawJob(@Nonnull PeripheralJob job)
      throws IllegalArgumentException;
}
