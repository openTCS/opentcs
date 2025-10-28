// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.rmi.services;

import java.rmi.RemoteException;
import java.util.Set;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.AcceptableOrderType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.Vehicle.EnergyLevelThresholdSet;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;
import org.opentcs.drivers.vehicle.management.VehicleAttachmentInformation;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;

/**
 * The default implementation of the vehicle service.
 * Delegates method invocations to the corresponding remote service.
 */
class RemoteVehicleServiceProxy
    extends
      RemoteTCSObjectServiceProxy<RemoteVehicleService>
    implements
      VehicleService {

  /**
   * Creates a new instance.
   */
  RemoteVehicleServiceProxy() {
  }

  @Override
  public void attachCommAdapter(
      TCSObjectReference<Vehicle> ref,
      VehicleCommAdapterDescription description
  )
      throws ObjectUnknownException,
        KernelRuntimeException {
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
      throws ObjectUnknownException,
        KernelRuntimeException {
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
      throws ObjectUnknownException,
        KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().enableCommAdapter(getClientId(), ref);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public VehicleAttachmentInformation fetchAttachmentInformation(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException,
        KernelRuntimeException {
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
      throws ObjectUnknownException,
        KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().fetchProcessModel(getClientId(), ref);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void sendCommAdapterMessage(
      TCSObjectReference<Vehicle> ref,
      VehicleCommAdapterMessage message
  )
      throws ObjectUnknownException,
        KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().sendCommAdapterMessage(getClientId(), ref, message);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void updateVehicleIntegrationLevel(
      TCSObjectReference<Vehicle> ref,
      Vehicle.IntegrationLevel integrationLevel
  )
      throws ObjectUnknownException,
        KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().updateVehicleIntegrationLevel(getClientId(), ref, integrationLevel);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void updateVehiclePaused(TCSObjectReference<Vehicle> ref, boolean paused)
      throws ObjectUnknownException,
        KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().updateVehiclePaused(getClientId(), ref, paused);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void updateVehicleEnergyLevelThresholdSet(
      TCSObjectReference<Vehicle> ref,
      EnergyLevelThresholdSet energyLevelThresholdSet
  )
      throws ObjectUnknownException,
        KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().updateVehicleEnergyLevelThresholdSet(
          getClientId(),
          ref,
          energyLevelThresholdSet
      );
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void updateVehicleAcceptableOrderTypes(
      TCSObjectReference<Vehicle> ref,
      Set<AcceptableOrderType> acceptableOrderTypes
  )
      throws ObjectUnknownException,
        KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().updateVehicleAcceptableOrderTypes(
          getClientId(),
          ref,
          acceptableOrderTypes
      );
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void updateVehicleEnvelopeKey(TCSObjectReference<Vehicle> ref, String envelopeKey)
      throws ObjectUnknownException,
        KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().updateVehicleEnvelopeKey(getClientId(), ref, envelopeKey);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
}
