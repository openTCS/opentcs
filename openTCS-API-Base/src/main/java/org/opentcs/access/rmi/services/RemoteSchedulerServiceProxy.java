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
import org.opentcs.access.SchedulerAllocationState;
import org.opentcs.components.kernel.services.SchedulerService;

/**
 * The default implementation of the scheduler service.
 * Delegates method invocations to the corresponding remote service.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
class RemoteSchedulerServiceProxy
    extends AbstractRemoteServiceProxy<RemoteSchedulerService>
    implements SchedulerService {

  @Override
  public SchedulerAllocationState fetchSchedulerAllocations()
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().fetchSchedulerAllocations(getClientId());
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
}
