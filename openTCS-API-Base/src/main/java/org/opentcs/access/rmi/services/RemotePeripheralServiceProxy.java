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
import org.opentcs.components.kernel.services.PeripheralService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.drivers.peripherals.PeripheralAdapterCommand;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;
import org.opentcs.drivers.peripherals.management.PeripheralAttachmentInformation;

/**
 * The default implementation of the vehicle service.
 * Delegates method invocations to the corresponding remote service.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
class RemotePeripheralServiceProxy
    extends RemoteTCSObjectServiceProxy<RemotePeripheralService>
    implements PeripheralService {

  @Override
  public void attachCommAdapter(TCSResourceReference<Location> ref,
                                PeripheralCommAdapterDescription description)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().attachCommAdapter(getClientId(), ref, description);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void disableCommAdapter(TCSResourceReference<Location> ref)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().disableCommAdapter(getClientId(), ref);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void enableCommAdapter(TCSResourceReference<Location> ref)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().enableCommAdapter(getClientId(), ref);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public PeripheralAttachmentInformation fetchAttachmentInformation(
      TCSResourceReference<Location> ref)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().fetchAttachmentInformation(getClientId(), ref);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public PeripheralProcessModel fetchProcessModel(TCSResourceReference<Location> ref)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().fetchProcessModel(getClientId(), ref);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void sendCommAdapterCommand(TCSResourceReference<Location> ref,
                                     PeripheralAdapterCommand command)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().sendCommAdapterCommand(getClientId(), ref, command);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
}
