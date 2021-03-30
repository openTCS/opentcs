/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.services;

import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * Declares the methods the peripheral service must provide which are not accessible to remote
 * peers.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface InternalPeripheralService
    extends PeripheralService {

  /**
   * Updates a peripheral's processing state.
   *
   * @param ref A reference to the location to be modified.
   * @param state The peripheral's new processing state.
   * @throws ObjectUnknownException If the referenced location does not exist.
   */
  void updatePeripheralProcState(TCSResourceReference<Location> ref,
                                 PeripheralInformation.ProcState state)
      throws ObjectUnknownException;

  /**
   * Updates a peripheral's reservation token.
   *
   * @param ref A reference to the location to be modified.
   * @param reservationToken The peripheral's new reservation token.
   * @throws ObjectUnknownException If the referenced location does not exist.
   */
  void updatePeripheralReservationToken(TCSResourceReference<Location> ref,
                                        String reservationToken)
      throws ObjectUnknownException;

  /**
   * Updates a peripheral's state.
   *
   * @param ref A reference to the location to be modified.
   * @param state The peripheral's new state.
   * @throws ObjectUnknownException If the referenced location does not exist.
   */
  void updatePeripheralState(TCSResourceReference<Location> ref,
                             PeripheralInformation.State state)
      throws ObjectUnknownException;

  /**
   * Updates a peripheral's current peripheral job.
   *
   * @param ref A reference to the location to be modified.
   * @param peripheralJob A reference to the peripheral job the peripheral device processes.
   * @throws ObjectUnknownException If the referenced location does not exist.
   */
  void updatePeripheralJob(TCSResourceReference<Location> ref,
                           TCSObjectReference<PeripheralJob> peripheralJob)
      throws ObjectUnknownException;
}
