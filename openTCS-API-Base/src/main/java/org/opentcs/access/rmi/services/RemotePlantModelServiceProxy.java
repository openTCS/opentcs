/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.services;

import java.rmi.RemoteException;
import java.util.Map;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.PlantModel;

/**
 * The default implementation of the plant model service.
 * Delegates method invocations to the corresponding remote service.
 */
class RemotePlantModelServiceProxy
    extends RemoteTCSObjectServiceProxy<RemotePlantModelService>
    implements PlantModelService {

  /**
   * Creates a new instance.
   */
  RemotePlantModelServiceProxy() {
  }

  @Override
  public PlantModel getPlantModel()
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().getPlantModel(getClientId());
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void createPlantModel(PlantModelCreationTO to)
      throws ObjectUnknownException, ObjectExistsException, KernelRuntimeException,
             IllegalStateException {
    checkServiceAvailability();

    try {
      getRemoteService().createPlantModel(getClientId(), to);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public String getModelName()
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().getModelName(getClientId());
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public Map<String, String> getModelProperties()
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().getModelProperties(getClientId());
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void updateLocationLock(TCSObjectReference<Location> ref, boolean locked)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().updateLocationLock(getClientId(), ref, locked);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void updatePathLock(TCSObjectReference<Path> ref, boolean locked)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().updatePathLock(getClientId(), ref, locked);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
}
