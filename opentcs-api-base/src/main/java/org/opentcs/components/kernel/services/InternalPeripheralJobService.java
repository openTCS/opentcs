// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel.services;

import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * Declares the methods the peripheral job service must provide which are not accessible to remote
 * peers.
 */
public interface InternalPeripheralJobService
    extends
      PeripheralJobService {

  /**
   * Updates a peripheral job's state.
   * Note that peripheral job states are intended to be manipulated by the peripheral job
   * dispatcher only.
   * Calling this method from any other parts of the kernel may result in undefined behaviour.
   *
   * @param ref A reference to the peripheral job to be modified.
   * @param state The peripheral job's new state.
   * @throws ObjectUnknownException If the referenced peripheral job does not exist.
   */
  void updatePeripheralJobState(TCSObjectReference<PeripheralJob> ref, PeripheralJob.State state)
      throws ObjectUnknownException;
}
