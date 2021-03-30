/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.services;

import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.peripherals.PeripheralJobCreationTO;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * Provides methods concerning {@link PeripheralJob}s.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface PeripheralJobService
    extends TCSObjectService {

  /**
   * Creates a peripheral job.
   * A new peripheral job is created with a generated unique ID and all other attributes taken from
   * the given transfer object.
   * A copy of the newly created transport order is then returned.
   *
   * @param to Describes the peripheral job to be created.
   * @return A copy of the newly created peripheral job.
   * @throws ObjectUnknownException If any referenced object does not exist.
   * @throws ObjectExistsException If an object with the same name already exists in the model.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  PeripheralJob createPeripheralJob(PeripheralJobCreationTO to)
      throws ObjectUnknownException, ObjectExistsException, KernelRuntimeException;
}
