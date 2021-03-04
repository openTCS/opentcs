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
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;

/**
 * The default implementation of the dispatcher service.
 * Delegates method invocations to the corresponding remote service.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
class RemoteDispatcherServiceProxy
    extends AbstractRemoteServiceProxy<RemoteDispatcherService>
    implements DispatcherService {

  @Override
  public void dispatch()
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().dispatch(getClientId());
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  @Deprecated
  public void releaseVehicle(TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().releaseVehicle(getClientId(), vehicleRef);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  @Deprecated
  public void withdrawByVehicle(TCSObjectReference<Vehicle> vehicleRef,
                                boolean immediateAbort,
                                boolean disableVehicle)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().withdrawByVehicle(getClientId(),
                                           vehicleRef,
                                           immediateAbort,
                                           disableVehicle);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  @Deprecated
  public void withdrawByTransportOrder(TCSObjectReference<TransportOrder> ref,
                                       boolean immediateAbort,
                                       boolean disableVehicle)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().withdrawByTransportOrder(getClientId(),
                                                  ref,
                                                  immediateAbort,
                                                  disableVehicle);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void withdrawByVehicle(TCSObjectReference<Vehicle> vehicleRef,
                                boolean immediateAbort)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().withdrawByVehicle(getClientId(),
                                           vehicleRef,
                                           immediateAbort);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void withdrawByTransportOrder(TCSObjectReference<TransportOrder> ref,
                                       boolean immediateAbort)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().withdrawByTransportOrder(getClientId(),
                                                  ref,
                                                  immediateAbort);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
}
