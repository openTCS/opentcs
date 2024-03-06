/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.services;

import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.PeripheralJobDispatcher;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * Provides methods concerning the {@link PeripheralJobDispatcher}.
 */
public interface PeripheralDispatcherService {

  /**
   * Explicitly trigger the dispatching process for peripheral jobs.
   * <p>
   * If called within the kernel application, this method is supposed to be called only on the
   * kernel executor thread.
   * </p>
   *
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void dispatch()
      throws KernelRuntimeException;

  /**
   * Withdraw any job that a peripheral device (represented by the given location) might be
   * processing.
   * <p>
   * If called within the kernel application, this method is supposed to be called only on the
   * kernel executor thread.
   * </p>
   *
   * @param ref A reference to the location representing the peripheral device.
   * @throws ObjectUnknownException If the referenced location does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void withdrawByLocation(TCSResourceReference<Location> ref)
      throws ObjectUnknownException, KernelRuntimeException;

  /**
   * Withdraw the given peripheral job.
   * <p>
   * If called within the kernel application, this method is supposed to be called only on the
   * kernel executor thread.
   * </p>
   *
   * @param ref A reference to the peripheral job to be withdrawn.
   * @throws ObjectUnknownException If the referenced peripheral job does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void withdrawByPeripheralJob(TCSObjectReference<PeripheralJob> ref)
      throws ObjectUnknownException, KernelRuntimeException;
}
