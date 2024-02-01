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
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.TransportOrder;

/**
 * The default implementation of the dispatcher service.
 * Delegates method invocations to the corresponding remote service.
 */
class RemoteDispatcherServiceProxy
    extends AbstractRemoteServiceProxy<RemoteDispatcherService>
    implements DispatcherService {

  /**
   * Creates a new instance.
   */
  RemoteDispatcherServiceProxy() {
  }

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

  @Override
  public void reroute(TCSObjectReference<Vehicle> ref, ReroutingType reroutingType)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().reroute(getClientId(), ref, reroutingType);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void rerouteAll(ReroutingType reroutingType) {
    checkServiceAvailability();

    try {
      getRemoteService().rerouteAll(getClientId(), reroutingType);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void assignNow(TCSObjectReference<TransportOrder> ref)
      throws KernelRuntimeException {
    checkServiceAvailability();
    try {
      getRemoteService().assignNow(getClientId(), ref);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
}
