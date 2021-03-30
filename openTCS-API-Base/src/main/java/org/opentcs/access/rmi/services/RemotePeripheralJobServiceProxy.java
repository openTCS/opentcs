/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.services;

import java.rmi.RemoteException;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.peripherals.PeripheralJobCreationTO;
import org.opentcs.components.kernel.services.PeripheralJobService;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * The default implementation of the peripheral job service.
 * Delegates method invocations to the corresponding remote service.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
class RemotePeripheralJobServiceProxy
    extends RemoteTCSObjectServiceProxy<RemotePeripheralJobService>
    implements PeripheralJobService {

  @Override
  public PeripheralJob createPeripheralJob(PeripheralJobCreationTO to)
      throws ObjectUnknownException, ObjectExistsException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().createPeripheralJob(getClientId(), to);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
}
