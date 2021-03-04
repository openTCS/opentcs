/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.services;

import java.rmi.RemoteException;
import java.util.Set;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.AttachmentInformation;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;

/**
 * The default implementation of the vehicle service.
 * Delegates method invocations to the corresponding remote service.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
class RemoteVehicleServiceProxy
    extends RemoteTCSObjectServiceProxy<RemoteVehicleService>
    implements VehicleService {

  @Override
  public void attachCommAdapter(TCSObjectReference<Vehicle> ref,
                                VehicleCommAdapterDescription description)
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
  public void disableCommAdapter(TCSObjectReference<Vehicle> ref)
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
  public void enableCommAdapter(TCSObjectReference<Vehicle> ref)
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
  public AttachmentInformation fetchAttachmentInformation(TCSObjectReference<Vehicle> ref)
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
  public VehicleProcessModelTO fetchProcessModel(TCSObjectReference<Vehicle> ref)
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
  public void sendCommAdapterCommand(TCSObjectReference<Vehicle> ref, AdapterCommand command)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().sendCommAdapterCommand(getClientId(), ref, command);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void sendCommAdapterMessage(TCSObjectReference<Vehicle> ref, Object message)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().sendCommAdapterMessage(getClientId(), ref, message);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void updateVehicleIntegrationLevel(TCSObjectReference<Vehicle> ref, 
                                            Vehicle.IntegrationLevel integrationLevel)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().updateVehicleIntegrationLevel(getClientId(), ref, integrationLevel);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void updateVehicleProcessableCategories(TCSObjectReference<Vehicle> ref,
                                                 Set<String> processableCategories)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().updateVehicleProcessableCategories(getClientId(),
                                                            ref,
                                                            processableCategories);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
}
