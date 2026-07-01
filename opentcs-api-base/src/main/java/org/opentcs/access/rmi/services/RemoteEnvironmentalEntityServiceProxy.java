// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.rmi.services;

import java.rmi.RemoteException;
import org.opentcs.access.to.model.EnvironmentalEntityCreationTO;
import org.opentcs.components.kernel.services.EnvironmentalEntityService;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.EnvironmentalEntity;
import org.opentcs.data.model.Pose;

/**
 * The default implementation of the environmental entity service.
 * Delegates method invocations to the corresponding remote service.
 */
public class RemoteEnvironmentalEntityServiceProxy
    extends
      RemoteTCSObjectServiceProxy<RemoteEnvironmentalEntityService>
    implements
      EnvironmentalEntityService {

  /**
   * Creates a new instance.
   */
  public RemoteEnvironmentalEntityServiceProxy() {
  }

  @Override
  public EnvironmentalEntity createEnvironmentalEntity(EnvironmentalEntityCreationTO to)
      throws ObjectExistsException {
    checkServiceAvailability();

    try {
      return getRemoteService().createEnvironmentalEntity(getClientId(), to);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void updateEnvironmentalEntityEnvelope(
      TCSObjectReference<EnvironmentalEntity> ref,
      Envelope envelope
  )
      throws ObjectUnknownException {
    checkServiceAvailability();

    try {
      getRemoteService().updateEnvironmentalEntityEnvelope(getClientId(), ref, envelope);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void updateEnvironmentalEntityPose(TCSObjectReference<EnvironmentalEntity> ref, Pose pose)
      throws ObjectUnknownException {
    checkServiceAvailability();

    try {
      getRemoteService().updateEnvironmentalEntityPose(getClientId(), ref, pose);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void updateEnvironmentalEntityIntegrationLevel(
      TCSObjectReference<EnvironmentalEntity> ref,
      EnvironmentalEntity.IntegrationLevel integrationLevel
  )
      throws ObjectUnknownException {
    checkServiceAvailability();

    try {
      getRemoteService().updateEnvironmentalEntityIntegrationLevel(
          getClientId(),
          ref,
          integrationLevel
      );
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void markEnvironmentalEntityRetired(TCSObjectReference<EnvironmentalEntity> ref)
      throws ObjectUnknownException {
    checkServiceAvailability();

    try {
      getRemoteService().markEnvironmentalEntityRetired(getClientId(), ref);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
}
